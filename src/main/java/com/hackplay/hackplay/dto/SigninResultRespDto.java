package com.hackplay.hackplay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SigninResultRespDto {
    private SigninRespDto signinRespDto;
    private String refreshToken;
}