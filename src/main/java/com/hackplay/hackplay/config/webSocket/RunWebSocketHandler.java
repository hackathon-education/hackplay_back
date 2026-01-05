package com.hackplay.hackplay.config.webSocket;

import com.hackplay.hackplay.service.ContainerActivityTracker;
import com.hackplay.hackplay.service.ProjectContainerService;
import com.hackplay.hackplay.service.ProjectRunCommandResolver;
import com.hackplay.hackplay.service.ProjectService;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunWebSocketHandler extends TextWebSocketHandler {

    private final ProjectContainerService containerService;
    private final ContainerActivityTracker activityTracker;
    private final ProjectRunCommandResolver commandResolver;
    private final ProjectService projectService;

    private final Map<String, PtyProcess> processes = new ConcurrentHashMap<>();
    private final Map<String, Thread> readers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String uuid = (String) session.getAttributes().get("uuid");
        Long projectId = (Long) session.getAttributes().get("projectId");

        if (uuid == null || projectId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("missing session attributes"));
            return;
        }

        // ✅ 컨테이너 보장 (1회만)
        containerService.ensureRunning(uuid);
        activityTracker.markActive(uuid);

        // ✅ 프로젝트 템플릿 조회
        String template = projectService.getTemplateById(projectId);

        // ✅ 실행 명령 결정
        String command = commandResolver.resolve(template);
        String containerName = "hackplay-project-" + uuid;

        // ✅ Run 실행
        PtyProcess process = new PtyProcessBuilder(
            new String[]{
                "docker", "exec", "-it",
                containerName,
                "bash", "-lc", command
            }
        )
        .setRedirectErrorStream(true)
        .start();

        processes.put(session.getId(), process);

        Thread reader = new Thread(
            () -> readLoop(session, process),
            "run-reader-" + session.getId()
        );
        reader.setDaemon(true);
        reader.start();
        readers.put(session.getId(), reader);

        session.sendMessage(new TextMessage(
            "\u001b[36m[Run Started]\u001b[0m\r\n" +
            "\u001b[90mTemplate: " + template + "\u001b[0m\r\n" +
            "\u001b[90mCommand: " + command + "\u001b[0m\r\n"
        ));

        // ✅ 포트 감지 (옵션)
        new Thread(() -> detectPort(session, containerName), "port-detector-" + uuid)
            .start();
    }

    private void detectPort(WebSocketSession session, String containerName) {
        try {
            Thread.sleep(1500);

            Process p = new ProcessBuilder(
                "docker", "exec", containerName, "detect-port.sh"
            )
            .redirectErrorStream(true)
            .start();

            String port = new String(p.getInputStream().readAllBytes()).trim();

            if (!port.isBlank() && session.isOpen()) {
                session.sendMessage(new TextMessage(
                    "\u001b[32m[PORT DETECTED] " + port + "\u001b[0m\r\n"
                ));
            }

        } catch (Exception e) {
            log.warn("❌ port detection failed: {}", e.getMessage());
        }
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
        } catch (Exception ignored) {}
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        if (!"STOP".equalsIgnoreCase(message.getPayload().trim())) return;

        PtyProcess process = processes.get(session.getId());
        if (process == null || !process.isAlive()) return;

        activityTracker.markActive(
            (String) session.getAttributes().get("uuid")
        );

        session.sendMessage(new TextMessage(
            "\u001b[33m[Stopping...]\u001b[0m\r\n"
        ));

        process.destroy(); // ✅ Run만 종료
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        PtyProcess process = processes.remove(session.getId());
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }

        Thread reader = readers.remove(session.getId());
        if (reader != null) {
            reader.interrupt();
        }

        log.info("Run terminal closed: session={}", session.getId());
    }
}
