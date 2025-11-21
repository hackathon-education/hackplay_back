package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.domain.Submission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionDetailRespDto {

    private Long submissionId;
    private String projectName;
    private Integer week;
    private String status;
    private LocalDateTime submittedAt;

    public static SubmissionDetailRespDto from(Submission submission) {
        return SubmissionDetailRespDto.builder()
                .submissionId(submission.getId())
                .projectName(submission.getProject().getName())
                .week(submission.getWeek())
                .status(submission.getStatus().name())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
