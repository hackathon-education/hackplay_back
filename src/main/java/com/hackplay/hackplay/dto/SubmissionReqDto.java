package com.hackplay.hackplay.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionReqDto {

    @NotNull(message = "프로젝트 ID는 필수입니다.")
    private Long projectId;
}
