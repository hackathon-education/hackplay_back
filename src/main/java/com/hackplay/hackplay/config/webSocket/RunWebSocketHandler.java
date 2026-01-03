package com.hackplay.hackplay.config.webSocket;

import com.hackplay.hackplay.service.ProjectWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunWebSocketHandler extends TextWebSocketHandler {

    private final ProjectWorkspaceService workspaceService;

    private final Map<String, Process> runProcesses = new ConcurrentHashMap<>();
    private final Map<String, Thread> readerThreads = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uuid = (String) session.getAttributes().get("uuid");
        String projectIdStr = (String) session.getAttributes().get("projectId");

        if (uuid == null || projectIdStr == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("missing auth or projectId"));
            return;
        }

        long projectId;
        try {
            projectId = Long.parseLong(projectIdStr);
        } catch (NumberFormatException e) {
            session.close(CloseStatus.BAD_DATA.withReason("invalid projectId"));
            return;
        }

        // ✅ (권장) 여기서 반드시 "프로젝트 접근 권한"을 검증해야 함
        // workspaceService.validateProjectAccess(projectId, uuid);

        Path projectRoot = workspaceService.resolveProjectRoot(projectId);

        // ===== 실행 명령: 필요하면 여기만 프로젝트 템플릿에 맞게 수정 =====
        String cmd = "npm run dev";

        ProcessBuilder pb = new ProcessBuilder("bash", "-lc", cmd)
                .directory(projectRoot.toFile())
                .redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        env.put("FORCE_COLOR", "1");
        env.put("NODE_ENV", "development");

        session.sendMessage(new TextMessage("🚀 Starting project...\n"));
        session.sendMessage(new TextMessage("👤 User: " + uuid + "\n"));
        session.sendMessage(new TextMessage("📁 Workspace: " + projectRoot + "\n"));
        session.sendMessage(new TextMessage("▶ Command: " + cmd + "\n\n"));

        Process process = pb.start();
        runProcesses.put(session.getId(), process);

        Thread outThread = new Thread(() -> readOutput(session, process), "run-output-" + session.getId());
        outThread.setDaemon(true);
        outThread.start();
        readerThreads.put(session.getId(), outThread);

        Thread watcher = new Thread(() -> watchExit(session, process), "run-watcher-" + session.getId());
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
        } catch (Exception ignored) {}
    }

    private void watchExit(WebSocketSession session, Process process) {
        try {
            int code = process.waitFor();
            if (session.isOpen()) {
                session.sendMessage(new TextMessage("\n🔴 Process exited: " + code + "\n"));
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload().trim();
        if (!"STOP".equals(payload)) return;

        Process p = runProcesses.get(session.getId());
        if (p == null || !p.isAlive()) {
            session.sendMessage(new TextMessage("⚠️ No active process\n"));
            return;
        }

        session.sendMessage(new TextMessage("🛑 Stopping...\n"));
        killProcessTree(p);
        session.sendMessage(new TextMessage("✅ Stopped\n"));
    }

    private void killProcessTree(Process process) {
        try {
            long pid = process.pid();

            new ProcessBuilder("pkill", "-TERM", "-P", String.valueOf(pid)).start().waitFor(2, TimeUnit.SECONDS);
            new ProcessBuilder("kill", "-TERM", String.valueOf(pid)).start().waitFor(2, TimeUnit.SECONDS);

            if (process.isAlive()) {
                new ProcessBuilder("pkill", "-KILL", "-P", String.valueOf(pid)).start().waitFor(2, TimeUnit.SECONDS);
                new ProcessBuilder("kill", "-KILL", String.valueOf(pid)).start().waitFor(2, TimeUnit.SECONDS);
                process.destroyForcibly();
            }
        } catch (Exception e) {
            process.destroyForcibly();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Process p = runProcesses.remove(session.getId());
        if (p != null && p.isAlive()) {
            killProcessTree(p);
        }

        Thread t1 = readerThreads.remove(session.getId());
        if (t1 != null) t1.interrupt();

        Thread t2 = readerThreads.remove(session.getId() + ":watcher");
        if (t2 != null) t2.interrupt();
    }
}
