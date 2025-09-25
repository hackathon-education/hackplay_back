package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectoryUpdateReqDto {
    
    @NotBlank(message = "기존 경로는 필수입니다.")
    private String oldPath;

    @NotBlank(message = "새 경로는 필수입니다.")
    private String newPath;
}
