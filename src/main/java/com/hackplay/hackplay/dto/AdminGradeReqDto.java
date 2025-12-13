package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.CommonEnums;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminGradeReqDto {

    @NotNull(message = "채점 상태는 필수입니다.")
    private CommonEnums.SubmissionStatus status;
}
