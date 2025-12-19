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
import java.util.concurrent.TimeUnit;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, Process> runProcesses = new ConcurrentHashMap<>();
    private final Map<String, Thread> readerThreads = new ConcurrentHashMap<>();
    private final Map<String, Boolean> processHealthMap = new ConcurrentHashMap<>();

    private final ProjectRepository projectRepository;

    private enum ProjectType {
        SPRING_BOOT, NODE_JS, REACT, VUE, ANGULAR, NEXT_JS, PYTHON, UNKNOWN
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

        try {
            long projectId = Long.parseLong(projectIdStr);
            Project project = projectRepository.findById(projectId).orElse(null);
            
            if (project == null) {
                session.sendMessage(new TextMessage("❌ Project not found\n"));
                session.close(CloseStatus.SERVER_ERROR);
                return;
            }

            String projectUuid = project.getUuid();
            File projectDir = new File("../projects/" + projectUuid);
            
            if (!projectDir.exists()) {
                session.sendMessage(new TextMessage("❌ Project directory not found: " + projectDir.getAbsolutePath() + "\n"));
                session.close(CloseStatus.SERVER_ERROR);
                return;
            }

            ProjectType type = detectProjectType(projectDir);
            ProcessBuilder pb = createProcessBuilder(type, projectDir);

            if (pb == null) {
                session.sendMessage(new TextMessage("❌ Unsupported project type: " + type + "\n"));
                session.close(CloseStatus.SERVER_ERROR);
                return;
            }

            // 환경 변수 설정
            setupEnvironment(pb, type);

            session.sendMessage(new TextMessage("🚀 Starting " + type + " project...\n"));
            log.info("🚀 Starting project {} (type: {}) for session {}", projectUuid, type, session.getId());

            Process process = pb.start();
            runProcesses.put(session.getId(), process);
            processHealthMap.put(session.getId(), true);

            // STDOUT + STDERR 통합 읽기
            Thread outputThread = createOutputReaderThread(session, process);
            outputThread.start();
            readerThreads.put(session.getId(), outputThread);

            // 프로세스 종료 감지
            Thread processWatcher = createProcessWatcherThread(session, process, type);
            processWatcher.start();
            readerThreads.put(session.getId() + "-watcher", processWatcher);

        } catch (NumberFormatException e) {
            session.sendMessage(new TextMessage("❌ Invalid projectId format\n"));
            session.close(CloseStatus.BAD_DATA);
        } catch (Exception e) {
            log.error("Failed to start project for session {}: {}", session.getId(), e.getMessage(), e);
            session.sendMessage(new TextMessage("❌ Failed to start project: " + e.getMessage() + "\n"));
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 출력 읽기 스레드 생성
     */
    private Thread createOutputReaderThread(WebSocketSession session, Process process) {
        return new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null && session.isOpen() && 
                       Boolean.TRUE.equals(processHealthMap.get(session.getId()))) {
                    
                    // 색상 코드와 함께 출력
                    String coloredOutput = formatOutput(line);
                    session.sendMessage(new TextMessage(coloredOutput + "\n"));
                }

            } catch (Exception e) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage("⚠️ Output stream ended: " + e.getMessage() + "\n"));
                    } catch (Exception ignored) {}
                }
                log.debug("Output reader ended for session {}: {}", session.getId(), e.getMessage());
            } finally {
                processHealthMap.put(session.getId(), false);
            }
        }, "output-reader-" + session.getId());
    }

    /**
     * 프로세스 종료 감지 스레드
     */
    private Thread createProcessWatcherThread(WebSocketSession session, Process process, ProjectType type) {
        return new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                processHealthMap.put(session.getId(), false);
                
                if (session.isOpen()) {
                    String message = exitCode == 0 
                        ? "✅ " + type + " process finished successfully\n"
                        : "❌ " + type + " process exited with code: " + exitCode + "\n";
                    session.sendMessage(new TextMessage(message));
                }
                log.info("Process ended for session {} with exit code: {}", session.getId(), exitCode);
                
            } catch (InterruptedException e) {
                log.debug("Process watcher interrupted for session {}", session.getId());
            } catch (Exception e) {
                log.error("Process watcher error for session {}: {}", session.getId(), e.getMessage());
            }
        }, "process-watcher-" + session.getId());
    }

    /**
     * 환경 변수 설정
     */
    private void setupEnvironment(ProcessBuilder pb, ProjectType type) {
        Map<String, String> env = pb.environment();
        
        // 공통 환경 변수
        env.put("FORCE_COLOR", "1");
        env.put("NODE_ENV", "development");
        
        // 타입별 환경 변수
        switch (type) {
            case NODE_JS:
            case REACT:
            case VUE:
            case ANGULAR:
            case NEXT_JS:
                env.put("npm_config_color", "always");
                break;
            case SPRING_BOOT:
                env.put("SPRING_PROFILES_ACTIVE", "dev");
                break;
            case PYTHON:
                env.put("PYTHONUNBUFFERED", "1");
                env.put("PYTHONIOENCODING", "UTF-8");
                break;
        }
    }

    /**
     * 출력 포매팅 (로그 레벨별 색상 적용)
     */
    private String formatOutput(String line) {
        String lowerLine = line.toLowerCase();
        
        if (lowerLine.contains("error") || lowerLine.contains("exception") || lowerLine.contains("failed")) {
            return "\u001b[31m" + line + "\u001b[0m"; // 빨강
        } else if (lowerLine.contains("warn") || lowerLine.contains("warning")) {
            return "\u001b[33m" + line + "\u001b[0m"; // 노랑
        } else if (lowerLine.contains("info") || lowerLine.contains("started") || lowerLine.contains("ready")) {
            return "\u001b[32m" + line + "\u001b[0m"; // 초록
        } else if (lowerLine.contains("debug")) {
            return "\u001b[36m" + line + "\u001b[0m"; // 시안
        }
        
        return line; // 기본색
    }

    /* ============================================================
        STOP 메시지 처리
    ============================================================ */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload().trim();

        if (!"STOP".equals(payload)) {
            session.sendMessage(new TextMessage("⚠️ Unknown command: " + payload + "\n"));
            return;
        }

        log.warn("🛑 STOP requested for session {}", session.getId());

        Process process = runProcesses.get(session.getId());
        if (process == null || !process.isAlive()) {
            session.sendMessage(new TextMessage("⚠️ No active process to stop\n"));
            return;
        }

        try {
            processHealthMap.put(session.getId(), false);
            killProcessTree(process);
            session.sendMessage(new TextMessage("🛑 Process stopped successfully\n"));
            log.info("Process stopped for session {}", session.getId());
        } catch (Exception e) {
            session.sendMessage(new TextMessage("❌ Failed to stop process: " + e.getMessage() + "\n"));
            log.error("Failed to stop process for session {}: {}", session.getId(), e.getMessage());
        }
    }

    /* ============================================================
        프로세스 트리 종료 (리눅스 최적화)
    ============================================================ */
    private void killProcessTree(Process process) {
        try {
            long pid = process.pid();
            
            // 1. 자식 프로세스들 SIGTERM으로 우아하게 종료
            ProcessBuilder childTerminator = new ProcessBuilder("pkill", "-TERM", "-P", String.valueOf(pid));
            Process termProcess = childTerminator.start();
            termProcess.waitFor(3, TimeUnit.SECONDS);

            // 2. 부모 프로세스 SIGTERM
            ProcessBuilder parentTerminator = new ProcessBuilder("kill", "-TERM", String.valueOf(pid));
            Process termParent = parentTerminator.start();
            termParent.waitFor(2, TimeUnit.SECONDS);

            // 3. 아직 살아있으면 SIGKILL로 강제 종료
            if (process.isAlive()) {
                Thread.sleep(1000); // 1초 대기
                if (process.isAlive()) {
                    ProcessBuilder childKiller = new ProcessBuilder("pkill", "-KILL", "-P", String.valueOf(pid));
                    childKiller.start().waitFor(2, TimeUnit.SECONDS);
                    
                    ProcessBuilder parentKiller = new ProcessBuilder("kill", "-KILL", String.valueOf(pid));
                    parentKiller.start().waitFor(2, TimeUnit.SECONDS);
                    
                    process.destroyForcibly();
                }
            }

        } catch (Exception e) {
            log.error("Kill process tree error: {}", e.getMessage());
            // 최후 수단
            process.destroyForcibly();
        }
    }

    /* ============================================================
        연결 종료 시 정리
    ============================================================ */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        
        processHealthMap.put(sessionId, false);
        
        Process process = runProcesses.remove(sessionId);
        if (process != null && process.isAlive()) {
            killProcessTree(process);
        }

        // 스레드 정리
        Thread outputThread = readerThreads.remove(sessionId);
        Thread watcherThread = readerThreads.remove(sessionId + "-watcher");
        
        if (outputThread != null && outputThread.isAlive()) {
            outputThread.interrupt();
        }
        if (watcherThread != null && watcherThread.isAlive()) {
            watcherThread.interrupt();
        }

        processHealthMap.remove(sessionId);
        
        log.info("❌ Run session closed: {} ({})", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /* ============================================================
        프로젝트 타입 감지 (개선됨)
    ============================================================ */
    private ProjectType detectProjectType(File projectDir) {
        // Spring Boot 감지
        if (new File(projectDir, "gradlew").exists() || 
            new File(projectDir, "build.gradle").exists() ||
            new File(projectDir, "pom.xml").exists()) {
            return ProjectType.SPRING_BOOT;
        }

        // Python 프로젝트 감지
        if (new File(projectDir, "requirements.txt").exists() ||
            new File(projectDir, "pyproject.toml").exists() ||
            new File(projectDir, "setup.py").exists() ||
            hasFileWithExtension(projectDir, ".py")) {
            return ProjectType.PYTHON;
        }

        // Node.js 기반 프로젝트 감지
        File packageJson = new File(projectDir, "package.json");
        if (packageJson.exists()) {
            try {
                String content = Files.readString(packageJson.toPath());
                
                if (content.contains("\"next\"")) return ProjectType.NEXT_JS;
                if (content.contains("\"react\"")) return ProjectType.REACT;
                if (content.contains("\"vue\"")) return ProjectType.VUE;
                if (content.contains("\"@angular\"")) return ProjectType.ANGULAR;
                
                return ProjectType.NODE_JS;
                
            } catch (Exception e) {
                log.warn("Failed to read package.json: {}", e.getMessage());
                return ProjectType.NODE_JS;
            }
        }

        return ProjectType.UNKNOWN;
    }

    /**
     * 특정 확장자 파일 존재 확인
     */
    private boolean hasFileWithExtension(File dir, String extension) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* ============================================================
        타입별 실행 설정 (리눅스 전용)
    ============================================================ */
    private ProcessBuilder createProcessBuilder(ProjectType type, File dir) {
        switch (type) {
            case SPRING_BOOT:
                if (new File(dir, "gradlew").exists()) {
                    return new ProcessBuilder("bash", "-c", "./gradlew bootRun")
                            .directory(dir)
                            .redirectErrorStream(true);
                } else {
                    return new ProcessBuilder("bash", "-c", "mvn spring-boot:run")
                            .directory(dir)
                            .redirectErrorStream(true);
                }

            case REACT:
            case NEXT_JS:
                return new ProcessBuilder("bash", "-c", "npm run dev")
                        .directory(dir)
                        .redirectErrorStream(true);

            case VUE:
                return new ProcessBuilder("bash", "-c", "npm run serve")
                        .directory(dir)
                        .redirectErrorStream(true);

            case ANGULAR:
                return new ProcessBuilder("bash", "-c", "ng serve")
                        .directory(dir)
                        .redirectErrorStream(true);

            case NODE_JS:
                return new ProcessBuilder("bash", "-c", "npm start")
                        .directory(dir)
                        .redirectErrorStream(true);

            case PYTHON:
                if (new File(dir, "manage.py").exists()) {
                    // Django
                    return new ProcessBuilder("bash", "-c", "python manage.py runserver")
                            .directory(dir)
                            .redirectErrorStream(true);
                } else if (new File(dir, "app.py").exists()) {
                    // Flask
                    return new ProcessBuilder("bash", "-c", "python app.py")
                            .directory(dir)
                            .redirectErrorStream(true);
                } else {
                    // 일반 Python
                    return new ProcessBuilder("bash", "-c", "python main.py")
                            .directory(dir)
                            .redirectErrorStream(true);
                }

            default:
                return null;
        }
    }
}