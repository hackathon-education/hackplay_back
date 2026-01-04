package com.hackplay.hackplay.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectContainerService {

    private static final String IMAGE = "hackplay-runtime";
    private static final String PROJECT_BASE = "/projects";

    public String containerName(String uuid) {
        return "hackplay-project-" + uuid;
    }

    public void ensureRunning(String uuid) {
        String name = containerName(uuid);

        if (!exists(name)) {
            create(uuid);
        } else {
            start(name);
        }
    }

    private boolean exists(String name) {
        String out = exec("docker ps -a --format '{{.Names}}'");
        return out.lines().anyMatch(n -> n.equals(name));
    }

    private void create(String uuid) {
        String name = containerName(uuid);
        String projectDir = PROJECT_BASE + "/" + uuid;

        log.info("🚀 Creating container {}", name);

        exec(String.join(" ",
            "docker run -d",
            "--name", name,

            // 리소스 제한
            "--memory=1g",
            "--cpus=1",
            "--pids-limit=256",

            // 보안 옵션
            "--cap-drop ALL",
            "--security-opt no-new-privileges",

            // ✅ 네트워크 허용 (중요)
            "--network bridge",

            // 프로젝트 볼륨
            "-v", projectDir + ":/projects",

            IMAGE
        ));
    }

    private void start(String name) {
        exec("docker start " + name);
    }

    public void stop(String uuid) {
        String name = containerName(uuid);
        exec("docker stop " + name);
    }

    private String exec(String cmd) {
        try {
            Process p = new ProcessBuilder("bash", "-c", cmd)
                    .redirectErrorStream(true)
                    .start();
            return new String(p.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
