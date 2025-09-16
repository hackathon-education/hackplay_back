package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileCreateReqDto {
    @NotNull(message = "디렉토리 ID는 필수입니다.")
    private Long dirId;

    @NotBlank(message = "파일명은 비어 있을 수 없습니다.")
    @Size(max = 255, message = "파일명은 255자를 초과할 수 없습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "파일명은 영문, 숫자, '.', '_', '-'만 허용됩니다.")
    private String name;

    @NotBlank(message = "파일 내용은 비어 있을 수 없습니다.")
    @Size(max = 1048576, message = "파일 크기는 1MB를 초과할 수 없습니다.")
    private String content;
}
