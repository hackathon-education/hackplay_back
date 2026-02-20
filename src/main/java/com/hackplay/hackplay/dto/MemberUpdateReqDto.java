package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.enums.member.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberUpdateReqDto {
    
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣_]{2,20}$", 
            message = "닉네임은 2-20자의 알파벳, 숫자, 한글, 언더스코어만 가능합니다.")
    private String nickname;
    
    private String currentPassword;
    
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[a-zA-Z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 최소 8자 이상, 영문/숫자/특수문자를 포함해야 합니다.")
    private String newPassword;
    
    private Role role;
}