package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileMoveReqDto {
    @NotBlank(message = "현재 경로는 필수입니다.")
    private String currentPath;

    @NotBlank(message = "이동할 경로는 필수입니다.")
    private String newParentDir;
}
