package com.hackplay.hackplay.common.enums.member;

public enum Auth {
    ADMIN,
    USER;

    // private final String description;

    // Auth(String description) {
    //     this.description = description;
    // }

    // public String getDescription() {
    //     return description;
    // }

    // public static Auth fromString(String value) {
    //     try {
    //         return Auth.valueOf(value.toUpperCase());
    //     } catch (IllegalArgumentException e) {
    //         throw new IllegalArgumentException("Invalid auth: " + value);
    //     }
    // }

    // 관리자 권한 확인
    public boolean isAdmin() {
        return this == ADMIN;
    }

    // 일반 사용자 권한 확인
    public boolean isUser() {
        return this == USER;
    }
}