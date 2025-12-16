package com.hackplay.hackplay.config.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LinuxTerminalWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, Process> processes = new ConcurrentHashMap<>();
    private final Map<String, BufferedWriter> writers = new ConcurrentHashMap<>();
    private final Map<String, Thread> readerThreads = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 리눅스 환경에서 bash 셸 시작
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-i")
                    .directory(new File(System.getProperty("user.dir")))
                    .redirectErrorStream(true);

            // 환경 변수 설정 (터미널 크기, 색상 지원 등)
            Map<String, String> env = pb.environment();
            env.put("TERM", "xterm-256color");
            env.put("COLUMNS", "120");
            env.put("LINES", "30");
            env.put("PS1", "\\u@\\h:\\w$ "); // 프롬프트 설정

            Process process = pb.start();

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
            );

            processes.put(session.getId(), process);
            writers.put(session.getId(), writer);

            // 출력 읽기 스레드
            Thread readerThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                    char[] buffer = new char[1024];
                    int bytesRead;
                    
                    while ((bytesRead = reader.read(buffer)) != -1 && session.isOpen()) {
                        String output = new String(buffer, 0, bytesRead);
                        session.sendMessage(new TextMessage(output));
                    }
                } catch (Exception e) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage("\r\n❌ 터미널 연결이 종료되었습니다.\r\n"));
                        } catch (Exception ignored) {}
                    }
                    log.warn("Terminal reader thread ended for session {}: {}", session.getId(), e.getMessage());
                }
            }, "terminal-reader-" + session.getId());

            readerThread.setDaemon(true);
            readerThread.start();
            readerThreads.put(session.getId(), readerThread);

            // 프로세스 종료 감지 스레드
            Thread processWatcher = new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(
                                "\r\n🔴 Shell exited with code: " + exitCode + "\r\n"));
                        session.close(CloseStatus.NORMAL);
                    }
                } catch (Exception e) {
                    log.debug("Process watcher ended for session {}: {}", session.getId(), e.getMessage());
                }
            }, "process-watcher-" + session.getId());

            processWatcher.setDaemon(true);
            processWatcher.start();

            // 연결 성공 메시지
            session.sendMessage(new TextMessage(
                    "\u001b[32m🚀 Linux Terminal Connected\u001b[0m\r\n" +
                    "\u001b[36mWorking Directory: " + System.getProperty("user.dir") + "\u001b[0m\r\n"
            ));

            log.info("🔥 Linux Terminal connected: {}", session.getId());

        } catch (Exception e) {
            log.error("Failed to establish terminal connection for session {}: {}", session.getId(), e.getMessage());
            session.sendMessage(new TextMessage("❌ 터미널 연결 실패: " + e.getMessage() + "\r\n"));
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Process process = processes.get(session.getId());
        BufferedWriter writer = writers.get(session.getId());

        if (process == null || !process.isAlive()) {
            session.sendMessage(new TextMessage("❌ 터미널 세션이 종료되었습니다.\r\n"));
            return;
        }

        try {
            String input = message.getPayload();

            // 특수 제어 문자 처리
            if (handleSpecialCommands(input, process, session)) {
                return;
            }

            // 일반 입력 전송
            writer.write(input);
            writer.flush();

        } catch (IOException e) {
            log.error("Failed to send input to terminal for session {}: {}", session.getId(), e.getMessage());
            session.sendMessage(new TextMessage("❌ 명령어 전송 실패\r\n"));
        }
    }

    /**
     * 특수 제어 문자 처리
     */
    private boolean handleSpecialCommands(String input, Process process, WebSocketSession session) throws IOException {
        switch (input) {
            case "\u0003": // Ctrl+C
                sendSignal(process, "SIGINT");
                session.sendMessage(new TextMessage("^C"));
                return true;

            case "\u001a": // Ctrl+Z
                sendSignal(process, "SIGTSTP");
                session.sendMessage(new TextMessage("^Z"));
                return true;

            case "\u0004": // Ctrl+D (EOF)
                try {
                    writers.get(session.getId()).close();
                } catch (Exception ignored) {}
                return true;

            case "clear\r": // clear 명령 최적화
            case "clear\n":
                session.sendMessage(new TextMessage("\u001b[2J\u001b[H"));
                return true;

            default:
                return false;
        }
    }

    /**
     * 프로세스에 시그널 전송 (리눅스)
     */
    private void sendSignal(Process process, String signal) {
        try {
            long pid = process.pid();
            ProcessBuilder signalBuilder = new ProcessBuilder("kill", "-" + signal, String.valueOf(pid));
            signalBuilder.start().waitFor(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to send {} to process: {}", signal, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        
        // 프로세스 정리
        Process process = processes.remove(sessionId);
        if (process != null && process.isAlive()) {
            try {
                // 자식 프로세스들까지 정리
                killProcessTree(process.pid());
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Error cleaning up process for session {}: {}", sessionId, e.getMessage());
            }
        }

        // Writer 정리
        BufferedWriter writer = writers.remove(sessionId);
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {}
        }

        // 스레드 정리
        Thread readerThread = readerThreads.remove(sessionId);
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }

        log.info("❌ Linux Terminal session closed: {} ({})", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * 프로세스 트리 전체 종료 (리눅스)
     */
    private void killProcessTree(long pid) {
        try {
            // 자식 프로세스들 먼저 종료
            ProcessBuilder childKiller = new ProcessBuilder("pkill", "-P", String.valueOf(pid));
            Process killProcess = childKiller.start();
            killProcess.waitFor(2, TimeUnit.SECONDS);

            // 부모 프로세스 종료
            ProcessBuilder parentKiller = new ProcessBuilder("kill", "-9", String.valueOf(pid));
            Process killParent = parentKiller.start();
            killParent.waitFor(2, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.warn("Failed to kill process tree for PID {}: {}", pid, e.getMessage());
        }
    }

    /**
     * 터미널 크기 변경 (클라이언트에서 resize 이벤트 처리용)
     */
    public void resizeTerminal(String sessionId, int cols, int rows) {
        Process process = processes.get(sessionId);
        if (process != null && process.isAlive()) {
            try {
                // stty를 이용한 터미널 크기 변경
                ProcessBuilder resizeBuilder = new ProcessBuilder(
                        "stty", "cols", String.valueOf(cols), "rows", String.valueOf(rows)
                );
                resizeBuilder.start().waitFor(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Failed to resize terminal for session {}: {}", sessionId, e.getMessage());
            }
        }
    }
}