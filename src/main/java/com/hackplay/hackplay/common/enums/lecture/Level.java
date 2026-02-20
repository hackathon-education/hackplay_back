package com.hackplay.hackplay.common.enums.lecture;

public enum Level {
    BASIC,
    INTERMEDIATE,
    ADVANCED;

    // 난이도 비교
    public boolean isEasierThan(Level other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isHarderThan(Level other) {
        return this.ordinal() > other.ordinal();
    }
}