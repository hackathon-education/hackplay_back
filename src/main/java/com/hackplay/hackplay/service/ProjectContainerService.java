package com.hackplay.hackplay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Service
public class ProjectContainerService {

    private static final String RUNTIME_IMAGE = "hackplay-runtime";

    // ⚠️ 반드시 실제 프로젝트 디렉터리 경로와 일치해야 함
    private static final String PROJECTS_HOST_PATH = "/home/ubuntu/Hackplay/projects";

    /* ==================================================
     * Public API
     * ================================================== */

    /**
     * 프로젝트 컨테이너 존재 + 실행 보장
     */
    public synchronized void ensureRunning(String projectUuid) {

        String name = containerName(projectUuid);

        try {
            if (exists(name)) {
                if (!isRunning(name)) {
                    log.info("▶️ Starting existing container {}", name);
                    start(name);
                }
                return;
            }

            log.info("🚀 Creating new project container {}", name);
            create(name, projectUuid);

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to ensure project container: " + name, e
            );
        }
    }

    /**
     * 컨테이너 중지 (GC/idle 정리용)
     */
    public synchronized void stop(String projectUuid) {

        String name = containerName(projectUuid);

        try {
            if (!exists(name)) return;

            if (isRunning(name)) {
                log.info("🛑 Stopping container {}", name);
                exec("docker", "stop", name);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to stop container: " + name, e);
        }
    }

    /**
     * 컨테이너 완전 제거 (운영 시 거의 사용 안 함)
     */
    public synchronized void remove(String projectUuid) {

        String name = containerName(projectUuid);

        try {
            if (exists(name)) {
                log.info("🧹 Removing container {}", name);
                exec("docker", "rm", "-f", name);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove container: " + name, e);
        }
    }

    /* ==================================================
     * Internal helpers
     * ================================================== */

    private boolean exists(String name) throws Exception {
        String out = exec(
            "docker", "ps", "-a",
            "--filter", "name=^/" + name + "$",
            "--format", "{{.Names}}"
        );
        return out.trim().equals(name);
    }

    private boolean isRunning(String name) throws Exception {
        String out = exec(
            "docker", "inspect",
            "-f", "{{.State.Running}}",
            name
        );
        return out.trim().equals("true");
    }

    private void create(String name, String uuid) throws Exception {

        String hostPath = PROJECTS_HOST_PATH + "/" + uuid;

        exec(
            "docker", "run", "-d",
            "--name", name,
            "-v", hostPath + ":/workspace",
            "-w", "/workspace",
            RUNTIME_IMAGE,
            "sleep", "infinity"
        );
    }

    private void start(String name) throws Exception {
        exec("docker", "start", name);
    }

    private String containerName(String uuid) {
        return "hackplay-project-" + uuid;
    }

    private String exec(String... cmd) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process p = pb.start();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }

        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException(
                "Command failed (" + exit + "): " + String.join(" ", cmd)
            );
        }

        return sb.toString();
    }
}
