package com.hackplay.hackplay.common.enums.submission;

public enum SubmissionStatus {
    NONE,
    PENDING,
    PASS,
    FAIL,
    REJECTED;

    // 상태 확인 헬퍼 메서드
    public boolean isNone() {
        return this == NONE;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isPass() {
        return this == PASS;
    }

    public boolean isFail() {
        return this == FAIL;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    // 최종 상태인지 확인 (변경 불가)
    public boolean isFinal() {
        return this == PASS || this == FAIL || this == REJECTED;
    }
}