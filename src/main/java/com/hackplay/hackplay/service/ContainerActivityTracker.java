package com.hackplay.hackplay.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ContainerActivityTracker {

    private final Map<String, Long> lastActive = new ConcurrentHashMap<>();

    public void markActive(String uuid) {
        lastActive.put(uuid, System.currentTimeMillis());
    }

    public Map<String, Long> snapshot() {
        return Map.copyOf(lastActive);
    }

    public void remove(String uuid) {
        lastActive.remove(uuid);
    }
}
