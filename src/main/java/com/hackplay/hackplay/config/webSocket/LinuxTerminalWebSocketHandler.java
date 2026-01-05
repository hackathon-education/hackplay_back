package com.hackplay.hackplay.config.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackplay.hackplay.service.ContainerActivityTracker;
import com.hackplay.hackplay.service.ProjectContainerService;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinuxTerminalWebSocketHandler extends TextWebSocketHandler {

    private final ProjectContainerService containerService;
    private final ContainerActivityTracker activityTracker;
    private final ObjectMapper objectMapper;

    private final Map<String, PtyProcess> processes = new ConcurrentHashMap<>();
    private final Map<String, Thread> readers = new ConcurrentHashMap<>();

    // 🔹 output batching
    private final Map<String, StringBuilder> outBuffers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService sender =
            Executors.newScheduledThreadPool(1);

    // 🔹 resize debounce
    private final ScheduledExecutorService resizeScheduler =
            Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> resizeTasks =
            new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String uuid = (String) session.getAttributes().get("uuid");
        if (uuid == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("missing uuid"));
            return;
        }

        containerService.ensureRunning(uuid);
        activityTracker.markActive(uuid);

        String containerName = "hackplay-project-" + uuid;

        PtyProcess process = new PtyProcessBuilder(
                new String[]{"docker", "exec", "-it", containerName, "bash"}
        )
        .setRedirectErrorStream(true)
        .start();

        processes.put(session.getId(), process);
        outBuffers.put(session.getId(), new StringBuilder());

        // 🔹 reader thread
        Thread reader = new Thread(
                () -> readLoop(session, process),
                "terminal-reader-" + session.getId()
        );
        reader.setDaemon(true);
        reader.start();
        readers.put(session.getId(), reader);

        // 🔹 sender (16ms batching)
        sender.scheduleAtFixedRate(() -> flushOutput(session),
                0, 16, TimeUnit.MILLISECONDS);

        session.sendMessage(new TextMessage(
                "\u001b[32m[Terminal Connected]\u001b[0m\r\n"
        ));
    }

    private void readLoop(WebSocketSession session, PtyProcess process) {
        try (InputStream in = process.getInputStream()) {
            byte[] buf = new byte[8192]; // 🔥 buffer 확장
            int n;
            while ((n = in.read(buf)) > 0 && session.isOpen()) {
                StringBuilder sb = outBuffers.get(session.getId());
                if (sb != null) {
                    sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            log.debug("terminal reader closed: {}", e.getMessage());
        }
    }

    private void flushOutput(WebSocketSession session) {
        try {
            StringBuilder sb = outBuffers.get(session.getId());
            if (sb != null && sb.length() > 0 && session.isOpen()) {
                session.sendMessage(new TextMessage(sb.toString()));
                sb.setLength(0);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();

        // ===============================
        // 1️⃣ resize (debounce)
        // ===============================
        if (payload.startsWith("{")) {
            ResizeMessage resize = objectMapper.readValue(payload, ResizeMessage.class);
            if ("resize".equals(resize.getType())) {

                ScheduledFuture<?> prev = resizeTasks.get(session.getId());
                if (prev != null) prev.cancel(false);

                resizeTasks.put(
                        session.getId(),
                        resizeScheduler.schedule(() -> {
                            PtyProcess p = processes.get(session.getId());
                            if (p != null && p.isAlive()) {
                                p.setWinSize(new WinSize(
                                        resize.getCols(),
                                        resize.getRows()
                                ));
                            }
                        }, 250, TimeUnit.MILLISECONDS)
                );
                return;
            }
        }

        // ===============================
        // 2️⃣ STOP
        // ===============================
        if ("STOP".equalsIgnoreCase(payload.trim())) {
            stopProcess(session);
            return;
        }

        // ===============================
        // 3️⃣ 일반 입력 (flush ❌)
        // ===============================
        PtyProcess process = processes.get(session.getId());
        if (process != null && process.isAlive()) {
            activityTracker.markActive(
                    (String) session.getAttributes().get("uuid")
            );
            process.getOutputStream().write(
                    payload.getBytes(StandardCharsets.UTF_8)
            );
            // ❌ flush 제거
        }
    }

    private void stopProcess(WebSocketSession session) {
        PtyProcess process = processes.get(session.getId());
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    @Data
    static class ResizeMessage {
        private String type;
        private int cols;
        private int rows;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        PtyProcess process = processes.remove(session.getId());
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }

        Thread reader = readers.remove(session.getId());
        if (reader != null) reader.interrupt();

        outBuffers.remove(session.getId());

        ScheduledFuture<?> resize = resizeTasks.remove(session.getId());
        if (resize != null) resize.cancel(false);

        log.info("Terminal closed: session={}", session.getId());
    }
}
