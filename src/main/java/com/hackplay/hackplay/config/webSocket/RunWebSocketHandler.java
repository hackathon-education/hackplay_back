package com.hackplay.hackplay.config.webSocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunWebSocketHandler extends TextWebSocketHandler {

    /**
     * 실행 중인 프로세스 (sessionId -> Process)
     */
    private final Map<String, Process> runProcesses = new ConcurrentHashMap<>();

    /**
     * 출력 읽기 스레드 (sessionId -> Thread)
     */
    private final Map<String, Thread> readerThreads = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        // ===== HandshakeInterceptor에서 주입된 값 =====
        String uuid = (String) session.getAttributes().get("uuid");
        Long projectId = (Long) session.getAttributes().get("projectId");
        Path projectRoot = (Path) session.getAttributes().get("projectRoot");

        if (uuid == null || projectId == null || projectRoot == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("missing auth or project context"));
            return;
        }

        // ===== 실행 명령 (필요 시 템플릿별로 분기 가능) =====
        // ⚠️ 사용자 입력으로 직접 명령을 받지 말 것
        String command = "npm run dev";

        ProcessBuilder pb = new ProcessBuilder(
                "bash",
                "-lc",
                command
        );

        pb.directory(projectRoot.toFile());
        pb.redirectErrorStream(true);

        // ===== 환경 변수 최소화 (도커 기준) =====
        Map<String, String> env = pb.environment();
        env.clear();
        env.put("PATH", "/usr/bin:/bin");
        env.put("NODE_ENV", "development");
        env.put("FORCE_COLOR", "1");

        session.sendMessage(new TextMessage(
                "🚀 Starting project\n" +
                "👤 User: " + uuid + "\n" +
                "📦 Project ID: " + projectId + "\n" +
                "📁 Workspace: " + projectRoot + "\n" +
                "▶ Command: " + command + "\n\n"
        ));

        // ===== 프로세스 시작 =====
        Process process = pb.start();
        runProcesses.put(session.getId(), process);

        // ===== stdout/stderr 읽기 스레드 =====
        Thread outThread = new Thread(
                () -> readOutput(session, process),
                "run-output-" + session.getId()
        );
        outThread.setDaemon(true);
        outThread.start();
        readerThreads.put(session.getId(), outThread);

        // ===== 종료 감시 스레드 =====
        Thread watcher = new Thread(
                () -> {
                    try {
                        watchExit(session, process);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                "run-watcher-" + session.getId()
        );
        watcher.setDaemon(true);
        watcher.start();
        readerThreads.put(session.getId() + ":watcher", watcher);
    }

    private void readOutput(WebSocketSession session, Process process) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null && session.isOpen()) {
                session.sendMessage(new TextMessage(line + "\n"));
            }
        } catch (Exception e) {
            log.debug("run output reader closed: {}", e.getMessage());
        }
    }

    private void watchExit(WebSocketSession session, Process process) throws IOException {
        try {
            int exitCode = process.waitFor();
            if (session.isOpen()) {
                session.sendMessage(
                        new TextMessage("\n🔴 Process exited with code: " + exitCode + "\n")
                );
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload().trim();

        // ===== 실행 중지 명령 =====
        if (!"STOP".equalsIgnoreCase(payload)) {
            return;
        }

        Process process = runProcesses.get(session.getId());
        if (process == null || !process.isAlive()) {
            session.sendMessage(new TextMessage("⚠️ No running process\n"));
            return;
        }

        session.sendMessage(new TextMessage("🛑 Stopping process...\n"));
        killProcessTree(process);
        session.sendMessage(new TextMessage("✅ Process stopped\n"));
    }

    private void killProcessTree(Process process) {
        try {
            long pid = process.pid();

            new ProcessBuilder("pkill", "-TERM", "-P", String.valueOf(pid))
                    .start()
                    .waitFor(2, TimeUnit.SECONDS);

            new ProcessBuilder("kill", "-TERM", String.valueOf(pid))
                    .start()
                    .waitFor(2, TimeUnit.SECONDS);

            if (process.isAlive()) {
                new ProcessBuilder("pkill", "-KILL", "-P", String.valueOf(pid))
                        .start()
                        .waitFor(2, TimeUnit.SECONDS);

                new ProcessBuilder("kill", "-KILL", String.valueOf(pid))
                        .start()
                        .waitFor(2, TimeUnit.SECONDS);

                process.destroyForcibly();
            }
        } catch (Exception e) {
            process.destroyForcibly();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        Process process = runProcesses.remove(session.getId());
        if (process != null && process.isAlive()) {
            killProcessTree(process);
        }

        Thread outThread = readerThreads.remove(session.getId());
        if (outThread != null) outThread.interrupt();

        Thread watcher = readerThreads.remove(session.getId() + ":watcher");
        if (watcher != null) watcher.interrupt();
    }
}
