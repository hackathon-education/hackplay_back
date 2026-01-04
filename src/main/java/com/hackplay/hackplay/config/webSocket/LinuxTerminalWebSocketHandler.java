package com.hackplay.hackplay.config.webSocket;

import com.hackplay.hackplay.service.ContainerActivityTracker;
import com.hackplay.hackplay.service.ProjectContainerService;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinuxTerminalWebSocketHandler extends TextWebSocketHandler {

    private final ProjectContainerService containerService;
    private final ContainerActivityTracker activityTracker;

    private final Map<String, PtyProcess> processes = new ConcurrentHashMap<>();
    private final Map<String, Thread> readers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String uuid = (String) session.getAttributes().get("uuid");
        if (uuid == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("missing uuid"));
            return;
        }

        // ✅ 컨테이너 보장
        containerService.ensureRunning(uuid);
        activityTracker.markActive(uuid);

        String containerName = "hackplay-project-" + uuid;

        // ✅ 컨테이너 bash 접속
        PtyProcess process = new PtyProcessBuilder(
                new String[]{
                        "docker", "exec",
                        "-it",
                        containerName,
                        "bash"
                }
        )
                .setRedirectErrorStream(true)
                .start();

        processes.put(session.getId(), process);

        Thread reader = new Thread(() -> readLoop(session, process),
                "terminal-reader-" + session.getId());
        reader.setDaemon(true);
        reader.start();
        readers.put(session.getId(), reader);

        session.sendMessage(new TextMessage(
                "\u001b[32m[Container Terminal Connected]\u001b[0m\r\n"
        ));
    }

    private void readLoop(WebSocketSession session, PtyProcess process) {
        try (InputStream in = process.getInputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1 && session.isOpen()) {
                session.sendMessage(
                        new TextMessage(new String(buf, 0, n, StandardCharsets.UTF_8))
                );
            }
        } catch (Exception e) {
            log.debug("terminal reader closed: {}", e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        PtyProcess process = processes.get(session.getId());
        if (process == null || !process.isAlive()) return;

        String uuid = (String) session.getAttributes().get("uuid");
        if (uuid != null) {
            activityTracker.markActive(uuid);
        }

        process.getOutputStream()
               .write(message.getPayload().getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().flush();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        PtyProcess process = processes.remove(session.getId());
        if (process != null) {
            process.destroyForcibly();
        }

        Thread reader = readers.remove(session.getId());
        if (reader != null) {
            reader.interrupt();
        }

        log.info("Terminal closed: session={}", session.getId());
    }
}
