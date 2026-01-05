package com.hackplay.hackplay.scheduler;

import com.hackplay.hackplay.service.ContainerActivityTracker;
import com.hackplay.hackplay.service.ProjectContainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContainerGcScheduler {

    private static final long IDLE_LIMIT_MS = 10 * 60 * 1000; // 10분

    private final ContainerActivityTracker tracker;
    private final ProjectContainerService containerService;

    @Scheduled(fixedDelay = 60_000)
    public void gcIdleContainers() {

        long now = System.currentTimeMillis();

        tracker.snapshot().forEach((uuid, lastActive) -> {
            if (now - lastActive > IDLE_LIMIT_MS) {
                log.info("🧹 stopping idle container: {}", uuid);
                containerService.stop(uuid);   // ✅ 여기서만 stop
                tracker.remove(uuid);
            }
        });
    }
}
