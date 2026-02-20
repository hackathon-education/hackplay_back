package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.common.enums.member.Role;
import com.hackplay.hackplay.common.enums.member.Status;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileRespDto {
    private String email;
    private String nickname;
    private boolean isEmailVerified;
    private String profileImageUrl;
    private Role role;
    private Status status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberProfileRespDto from(String email, String nickname, boolean isEmailVerified, String profileImageUrl, Role role, Status status, LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return MemberProfileRespDto.builder()
                .email(email)
                .nickname(nickname)
                .isEmailVerified(isEmailVerified)
                .profileImageUrl(profileImageUrl)
                .role(role)
                .status(status)
                .lastLoginAt(lastLoginAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}