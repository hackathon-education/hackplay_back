package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileUpdateReqDto {

    @NotBlank(message = "파일 내용은 비어 있을 수 없습니다.")
    @Size(max = 1048576, message = "파일 크기는 1MB를 초과할 수 없습니다.")
    private String content;
}
