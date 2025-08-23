package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.CommonEnums;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupReqDto {
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Pattern(
        regexp = "^[가-힣a-zA-Z\\s-]{2,30}$",
        message = "닉네임은 2~30자, 한글/영문/공백/하이픈만 사용할 수 있습니다."
    )
    private String nickname;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @NotNull(message = "직군은 필수입니다.")
    private CommonEnums.Role role;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~]).{8,20}$",
        message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;
    
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;
}
