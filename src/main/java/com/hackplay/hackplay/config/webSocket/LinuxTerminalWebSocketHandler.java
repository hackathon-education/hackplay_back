package com.hackplay.hackplay.config.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinuxTerminalWebSocketHandler extends TextWebSocketHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, PtyProcess> processes = new ConcurrentHashMap<>();
    private final Map<String, Thread> readerThreads = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String uuid = (String) session.getAttributes().get("uuid");
        Path projectRoot = (Path) session.getAttributes().get("projectRoot");

        if (uuid == null || projectRoot == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("missing auth or project root"));
            return;
        }

        Map<String, String> env = Map.of(
                "TERM", "xterm-256color",
                "PATH", "/usr/bin:/bin",
                "HOME", projectRoot.toString()
        );

        PtyProcess process = new PtyProcessBuilder(
                new String[]{"/bin/bash", "--login"}
        )
                .setDirectory(projectRoot.toString())
                .setEnvironment(env)
                .setRedirectErrorStream(true)
                .start();

        processes.put(session.getId(), process);

        Thread readerThread = new Thread(
                () -> readLoop(session, process),
                "pty-reader-" + session.getId()
        );
        readerThread.setDaemon(true);
        readerThread.start();
        readerThreads.put(session.getId(), readerThread);

        session.sendMessage(new TextMessage(
                "\u001b[32m[Terminal Connected]\u001b[0m\r\n"
        ));
    }

    /* =========================================================
       출력 읽기
    ========================================================= */
    private void readLoop(WebSocketSession session, PtyProcess process) {
        try (InputStream in = process.getInputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1 && session.isOpen()) {
                session.sendMessage(
                        new TextMessage(new String(buf, 0, len, StandardCharsets.UTF_8))
                );
            }
        } catch (Exception e) {
            log.debug("PTY reader closed: {}", e.getMessage());
        }
    }

    /* =========================================================
       입력 / resize 처리
    ========================================================= */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        PtyProcess process = processes.get(session.getId());
        if (process == null || !process.isAlive()) {
            return;
        }

        String payload = message.getPayload();

        // ---------- resize 메시지 ----------
        if (payload.startsWith("{")) {
            try {
                JsonNode json = MAPPER.readTree(payload);
                if ("resize".equals(json.path("type").asText())) {
                    int cols = json.path("cols").asInt();
                    int rows = json.path("rows").asInt();

                    if (cols > 0 && rows > 0) {
                        WinSize winSize = new WinSize(cols, rows);
                        process.setWinSize(winSize);
                    }
                    return;
                }
            } catch (Exception e) {
                // JSON 실패 시 일반 입력으로 fallback
            }
        }

        // ---------- 일반 터미널 입력 ----------
        OutputStream out = process.getOutputStream();
        out.write(payload.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cleanup(session.getId());
    }

    /* =========================================================
       정리
    ========================================================= */
    private void cleanup(String sessionId) {
        try {
            PtyProcess p = processes.remove(sessionId);
            if (p != null && p.isAlive()) {
                p.destroyForcibly();
            }

            Thread t = readerThreads.remove(sessionId);
            if (t != null) {
                t.interrupt();
            }
        } catch (Exception ignored) {}
    }
}
