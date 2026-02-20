package com.hackplay.hackplay.common.enums.lecture;

public enum WeekProgressStatus {
    COMPLETED,
    IN_PROGRESS,
    LOCKED;

    // private final String description;

    // WeekProgressStatus(String description) {
    //     this.description = description;
    // }

    // public String getDescription() {
    //     return description;
    // }

    // public static WeekProgressStatus fromString(String value) {
    //     try {
    //         return WeekProgressStatus.valueOf(value.toUpperCase());
    //     } catch (IllegalArgumentException e) {
    //         throw new IllegalArgumentException("Invalid progress status: " + value);
    //     }
    // }

    // 상태 확인 헬퍼 메서드
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isLocked() {
        return this == LOCKED;
    }
}