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

@Component
@Slf4j
@RequiredArgsConstructor
public class LinuxTerminalWebSocketHandler extends TextWebSocketHandler {

    private final ProjectWorkspaceService workspaceService;

    private final Map<String, Process> processes = new ConcurrentHashMap<>();
    private final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();
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
        // 위 메서드가 없다면 서비스/리포지토리 레벨에서 소유자 검증 추가 권장

        Path projectRoot = workspaceService.resolveProjectRoot(projectId);

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-i")
                .directory(projectRoot.toFile())
                .redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        env.put("TERM", "xterm-256color");
        env.put("PS1", "\\u@\\h:\\w$ ");

        Process process = pb.start();

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
        );

        processes.put(session.getId(), process);
        writers.put(session.getId(), writer);

        Thread readerThread = new Thread(() -> readOutput(session, process), "terminal-reader-" + session.getId());
        readerThread.setDaemon(true);
        readerThread.start();
        readerThreads.put(session.getId(), readerThread);

        session.sendMessage(new TextMessage(
                "\u001b[32m[Terminal Connected]\u001b[0m\r\n" +
                "\u001b[36mUser: " + uuid + "\u001b[0m\r\n" +
                "\u001b[36mWorkspace: " + projectRoot + "\u001b[0m\r\n"
        ));
    }

    private void readOutput(WebSocketSession session, Process process) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            char[] buf = new char[1024];
            int n;
            while ((n = reader.read(buf)) != -1 && session.isOpen()) {
                session.sendMessage(new TextMessage(new String(buf, 0, n)));
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Process process = processes.get(session.getId());
        BufferedWriter writer = writers.get(session.getId());

        if (process == null || writer == null || !process.isAlive()) {
            session.sendMessage(new TextMessage("❌ terminal closed\r\n"));
            return;
        }

        String input = message.getPayload();

        // Ctrl+C
        if ("\u0003".equals(input)) {
            sendSignal(process, "SIGINT");
            return;
        }

        writer.write(input);
        writer.flush();
    }

    private void sendSignal(Process process, String signal) {
        try {
            new ProcessBuilder("kill", "-" + signal, String.valueOf(process.pid()))
                    .start().waitFor(1, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cleanup(session.getId());
    }

    private void cleanup(String sessionId) {
        try {
            Process p = processes.remove(sessionId);
            if (p != null && p.isAlive()) {
                new ProcessBuilder("pkill", "-KILL", "-P", String.valueOf(p.pid())).start();
                new ProcessBuilder("kill", "-9", String.valueOf(p.pid())).start();
                p.destroyForcibly();
            }

            BufferedWriter w = writers.remove(sessionId);
            if (w != null) w.close();

            Thread t = readerThreads.remove(sessionId);
            if (t != null) t.interrupt();

        } catch (Exception ignored) {}
    }
}
