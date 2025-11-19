package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryMoveReqDto {
    
    @NotBlank(message = "기존 경로는 필수입니다.")
    private String currentPath;

    @NotBlank(message = "새 경로는 필수입니다.")
    private String newParentDir;
}
