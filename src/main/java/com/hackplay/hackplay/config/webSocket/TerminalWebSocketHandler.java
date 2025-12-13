package com.hackplay.hackplay.config.webSocket;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, PtyProcess> processes = new ConcurrentHashMap<>();
    private final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String os = System.getProperty("os.name").toLowerCase();
        String shell = os.contains("win") ? "cmd.exe" : "bash";

        log.info("🔥 Terminal connected: {} ({})", session.getId(), shell);

        // working directory → 반드시 String
        String workingDir = System.getProperty("user.dir");

        PtyProcess process = PtyProcess.exec(
                new String[]{shell},
                null,
                workingDir
        );

        process.setWinSize(new WinSize(120, 30));

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        );

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
        );

        processes.put(session.getId(), process);
        writers.put(session.getId(), writer);

        // 출력 스레드
        Thread outThread = new Thread(() -> {
            try {
                char[] buf = new char[2048];
                int len;
                while ((len = reader.read(buf)) != -1 && session.isOpen()) {
                    session.sendMessage(new TextMessage(new String(buf, 0, len)));
                }
            } catch (Exception ignore) {}

        }, "term-reader-" + session.getId());
        outThread.start();

        session.sendMessage(new TextMessage(
                "\u001b[32m[PTY Connected → " + shell + "]\u001b[0m\r\n"
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        PtyProcess process = processes.get(session.getId());
        BufferedWriter writer = writers.get(session.getId());

        if (process == null || !process.isAlive()) {
            session.sendMessage(new TextMessage("❌ 세션 종료됨\r\n"));
            return;
        }

        String input = msg.getPayload();

        // Ctrl+C
        if ("\u0003".equals(input)) {
            process.destroy();
            session.sendMessage(new TextMessage("프로세스 종료\r\n"));
            return;
        }

        writer.write(input);
        writer.flush();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        PtyProcess process = processes.remove(session.getId());
        writers.remove(session.getId());

        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }

        log.info("❌ Terminal session closed: {}", session.getId());
    }
}
