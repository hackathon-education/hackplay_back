package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DirectoryCreateReqDto {

    @NotBlank(message = "디렉토리명은 필수입니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "디렉토리명은 영문, 숫자, 밑줄(_), 하이픈(-)만 사용할 수 있습니다."
    )
    private String name;

    private Long parentId;
}
