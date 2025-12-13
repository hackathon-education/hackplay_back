package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryRenameReqDto {
    @NotBlank(message = "현재 경로는 필수입니다.")
    private String currentPath;

    @NotBlank(message = "새 폴더명은 필수입니다.")
    private String newName;
}