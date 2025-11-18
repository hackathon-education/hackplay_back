package com.hackplay.hackplay.config.webSocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.repository.ProjectRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, Process> runProcesses = new ConcurrentHashMap<>();
    private final Map<String, Thread> readerThreads = new ConcurrentHashMap<>();

    private final ProjectRepository projectRepository;

    private enum ProjectType {
        SPRING_BOOT, NODE_JS, REACT, VUE, ANGULAR, NEXT_JS, UNKNOWN
    }

    /* ============================================================
        연결 성립
    ============================================================ */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String projectIdStr = (String) session.getAttributes().get("projectId");

        if (projectIdStr == null || projectIdStr.isEmpty()) {
            session.sendMessage(new TextMessage("❌ projectId required\n"));
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        long projectId = Long.parseLong(projectIdStr);

        String projectUuid = projectRepository.findById(projectId)
                .map(Project::getUuid)
                .orElse(null);

        if (projectUuid == null) {
            session.sendMessage(new TextMessage("❌ Project not found\n"));
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }

        File projectDir = new File("../projects/" + projectUuid);
        if (!projectDir.exists()) {
            session.sendMessage(new TextMessage("❌ Project directory not found\n"));
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }

        String os = System.getProperty("os.name").toLowerCase();
        ProjectType type = detectProjectType(projectDir);
        ProcessBuilder pb = createProcessBuilder(type, projectDir, os);

        if (pb == null) {
            session.sendMessage(new TextMessage("❌ Unsupported project type\n"));
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }

        Process process = pb.start();
        runProcesses.put(session.getId(), process);

        /* ===================== STDOUT ===================== */
        Thread outThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (!session.isOpen()) break;
                    session.sendMessage(new TextMessage(line + "\n"));
                }

            } catch (Exception e) {
                log.error("[STDOUT] {}", e.getMessage());
            }
        });

        /* ===================== STDERR ===================== */
        Thread errThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (!session.isOpen()) break;
                    session.sendMessage(new TextMessage(line + "\n"));
                }

            } catch (Exception e) {
                log.error("[STDERR] {}", e.getMessage());
            }
        });

        outThread.start();
        errThread.start();

        readerThreads.put(session.getId(), outThread);
        readerThreads.put(session.getId() + "-err", errThread);

        /* ===================== 종료 감지 ===================== */
        new Thread(() -> {
            try {
                int code = process.waitFor();
                if (session.isOpen()) {
                    if (code == 0)
                        session.sendMessage(new TextMessage("✅ Process finished\n"));
                    else
                        session.sendMessage(new TextMessage("❌ Process exited: " + code + "\n"));
                }
            } catch (Exception ignored) {}
        }).start();
    }


    /* ============================================================
        STOP 메시지 처리
    ============================================================ */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();

        if (!"STOP".equals(payload)) return;

        log.warn("🛑 STOP requested for session {}", session.getId());

        Process process = runProcesses.get(session.getId());
        if (process == null || !process.isAlive()) {
            session.sendMessage(new TextMessage("⚠️ No active process\n"));
            return;
        }

        killProcessTree(process);

        session.sendMessage(new TextMessage("🛑 Process stopped\n"));
    }

    /* ============================================================
        전체 프로세스 종료 - 자식까지 (Windows + Linux + macOS)
    ============================================================ */
    private void killProcessTree(Process process) {

        try {
            long pid = process.pid();

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows - 자식 포함 강제 종료
                new ProcessBuilder("cmd.exe", "/c", "taskkill /PID " + pid + " /T /F")
                        .start()
                        .waitFor();
            } else {
                // macOS / Linux
                // 부모 + 자식 모두 kill
                new ProcessBuilder("bash", "-c", "pkill -TERM -P " + pid).start().waitFor();
                new ProcessBuilder("bash", "-c", "kill -9 " + pid).start().waitFor();
            }

            process.destroyForcibly();

        } catch (Exception e) {
            log.error("Kill error: {}", e.getMessage());
        }
    }


    /* ============================================================
        연결 종료 시
    ============================================================ */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        Process process = runProcesses.remove(session.getId());

        if (process != null && process.isAlive()) {
            killProcessTree(process);
        }

        Thread t1 = readerThreads.remove(session.getId());
        Thread t2 = readerThreads.remove(session.getId() + "-err");

        if (t1 != null) t1.interrupt();
        if (t2 != null) t2.interrupt();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /* ============================================================
        프로젝트 타입 감지
    ============================================================ */
    private ProjectType detectProjectType(File projectDir) {
        if (new File(projectDir, "gradlew").exists() || new File(projectDir, "build.gradle").exists())
            return ProjectType.SPRING_BOOT;

        if (new File(projectDir, "pom.xml").exists())
            return ProjectType.SPRING_BOOT;

        if (new File(projectDir, "package.json").exists()) {
            try {
                String content = Files.readString(new File(projectDir, "package.json").toPath());

                if (content.contains("next")) return ProjectType.NEXT_JS;
                if (content.contains("react")) return ProjectType.REACT;
                if (content.contains("vue")) return ProjectType.VUE;
                if (content.contains("@angular")) return ProjectType.ANGULAR;

                return ProjectType.NODE_JS;

            } catch (Exception e) {
                return ProjectType.NODE_JS;
            }
        }

        return ProjectType.UNKNOWN;
    }

    /* ============================================================
        타입별 실행 설정
    ============================================================ */
    private ProcessBuilder createProcessBuilder(ProjectType type, File dir, String os) {

        boolean isWin = os.contains("win");

        switch (type) {

            case SPRING_BOOT:
                return isWin
                        ? new ProcessBuilder("cmd.exe", "/c", "gradlew.bat bootRun").directory(dir)
                        : new ProcessBuilder("bash", "-c", "./gradlew bootRun").directory(dir);

            case REACT:
            case NEXT_JS:
                return isWin
                        ? new ProcessBuilder("cmd.exe", "/c", "npm run dev").directory(dir)
                        : new ProcessBuilder("bash", "-c", "npm run dev").directory(dir);

            case VUE:
                return isWin
                        ? new ProcessBuilder("cmd.exe", "/c", "npm run serve").directory(dir)
                        : new ProcessBuilder("bash", "-c", "npm run serve").directory(dir);

            case ANGULAR:
            case NODE_JS:
                return isWin
                        ? new ProcessBuilder("cmd.exe", "/c", "npm start").directory(dir)
                        : new ProcessBuilder("bash", "-c", "npm start").directory(dir);

            default:
                return null;
        }
    }
}
