package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.domain.Submission;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubmissionDetailRespDto {

    private Long submissionId;
    private String memberEmail;
    private String projectName;
    private Integer week;
    private String zipPath;
    private String status;
    private LocalDateTime submittedAt;

    public static AdminSubmissionDetailRespDto from(Submission submission) {
        return AdminSubmissionDetailRespDto.builder()
                .submissionId(submission.getId())
                .memberEmail(submission.getMember().getEmail())
                .projectName(submission.getProject().getName())
                .week(submission.getWeek())
                .zipPath(submission.getZipPath())
                .status(submission.getStatus().name())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
