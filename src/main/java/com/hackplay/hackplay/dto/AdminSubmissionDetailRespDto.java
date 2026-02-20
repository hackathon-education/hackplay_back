package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.domain.Project;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubmissionDetailRespDto {

    private Long projectId;
    private String memberEmail;
    private String projectName;
    private Integer week;
    private String zipPath;
    private String status;
    private LocalDateTime submittedAt;

    public static AdminSubmissionDetailRespDto from(Project project) {
        return AdminSubmissionDetailRespDto.builder()
                .projectId(project.getId())
                .memberEmail(project.getMember().getEmail())
                .projectName(project.getName())
                .week(project.getCurrentWeek())
                .zipPath(project.getZipPath())
                .status(project.getStatus().name())
                .submittedAt(project.getSubmittedAt())
                .build();
    }
}
