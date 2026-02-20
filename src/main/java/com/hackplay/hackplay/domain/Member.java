package com.hackplay.hackplay.domain;

import java.time.LocalDateTime;

import com.hackplay.hackplay.common.enums.member.Auth;
import com.hackplay.hackplay.common.enums.member.Role;
import com.hackplay.hackplay.common.enums.member.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth", nullable = false)
    private Auth auth = Auth.USER;

    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Member(Long id, String uuid , String email, String nickname,  String password, boolean isEmailVerified, String refreshToken, 
                Role role, Status status, LocalDateTime lastLoginAt, String profileImageUrl) {
        this.id = id;
        this.uuid = uuid;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.isEmailVerified = isEmailVerified;
        this.refreshToken = refreshToken;
        this.role = role;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 로그인 시 회원 리프레쉬 토큰 DB 저장 및 마지막 로그인 시점 저장.
    public void signinUpdate(String refreshToken){
        this.refreshToken = refreshToken;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void signoutUpdate() {
        this.refreshToken = null;
    }

    public void updateMemberInfo(String nickname, String profileImageUrl, Role role) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }
}