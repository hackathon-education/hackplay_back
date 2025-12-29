package com.hackplay.hackplay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReissueRespDto {
    private String accessToken;
    private String refreshToken;
}
