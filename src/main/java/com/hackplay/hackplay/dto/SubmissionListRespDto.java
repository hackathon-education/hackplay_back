package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.domain.Submission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionListRespDto {

    private Long submissionId;
    private String projectName;
    private Integer week;
    private String status;
    private LocalDateTime submittedAt;

    public static SubmissionListRespDto from(Submission submission) {
        return SubmissionListRespDto.builder()
                .submissionId(submission.getId())
                .week(submission.getWeek())
                .projectName(submission.getProject().getName())
                .status(submission.getStatus().toString())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
