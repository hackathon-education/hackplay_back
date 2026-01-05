package com.hackplay.hackplay.config.webSocket;

import com.hackplay.hackplay.service.*;
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
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunWebSocketHandler extends TextWebSocketHandler {

    private final ProjectContainerService containerService;
    private final ContainerActivityTracker activityTracker;
    private final ProjectRunCommandResolver commandResolver;
    private final ProjectService projectService;

    private final Map<String, PtyProcess> processes = new ConcurrentHashMap<>();
    private final Map<String, StringBuilder> outBuffers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService sender =
            Executors.newSingleThreadScheduledExecutor();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String uuid = (String) session.getAttributes().get("uuid");
        Long projectId = (Long) session.getAttributes().get("projectId");

        if (uuid == null || projectId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        containerService.ensureRunning(uuid);
        activityTracker.markActive(uuid);

        String template = projectService.getTemplateById(projectId);
        String command = commandResolver.resolve(template);
        String containerName = "hackplay-project-" + uuid;

        PtyProcess process = new PtyProcessBuilder(
                new String[]{"docker", "exec", "-it", containerName, "bash", "-lc", command}
        )
        .setRedirectErrorStream(true)
        .start();

        processes.put(session.getId(), process);
        outBuffers.put(session.getId(), new StringBuilder());

        new Thread(() -> readLoop(session, process)).start();

        sender.scheduleAtFixedRate(
                () -> flushOutput(session),
                0, 16, TimeUnit.MILLISECONDS
        );
    }

    private void readLoop(WebSocketSession session, PtyProcess process) {
        try (InputStream in = process.getInputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0 && session.isOpen()) {
                outBuffers.get(session.getId())
                        .append(new String(buf, 0, n, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {}
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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        PtyProcess p = processes.remove(session.getId());
        if (p != null) p.destroyForcibly();
        outBuffers.remove(session.getId());
    }
}
