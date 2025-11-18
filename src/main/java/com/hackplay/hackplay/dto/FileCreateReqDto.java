package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileCreateReqDto {
    @NotBlank(message = "파일명은 비어 있을 수 없습니다.")
    @Size(max = 255, message = "파일명은 255자를 초과할 수 없습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "파일명은 영문, 숫자, '.', '_', '-'만 허용됩니다.")
    private String name;

    // @NotBlank(message = "파일 내용은 필수입니다.")
    @Size(max = 1048576, message = "파일 크기는 1MB를 초과할 수 없습니다.")
    private String content;

    private String parentPath;
}
