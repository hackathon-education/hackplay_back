package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.domain.Member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SigninRespDto {
    private String nickname;
    private String email;
    private boolean isEmailVerified;
    private String profileImageUrl;
    private CommonEnums.Role role;

    public static SigninRespDto entityToDto(Member member) {
        return SigninRespDto.builder()
            .nickname(member.getNickname())
            .email(member.getEmail())
            .isEmailVerified(member.isEmailVerified())
            .profileImageUrl(member.getProfileImageUrl())
            .role(member.getRole())
            .build();
    }
}
