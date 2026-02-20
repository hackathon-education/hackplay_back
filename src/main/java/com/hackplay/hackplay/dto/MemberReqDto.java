package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.enums.member.Role;

import lombok.Data;

@Data
public class MemberReqDto {
    private String nickname;
    private String profileImageUrl;
    private Role role;
}