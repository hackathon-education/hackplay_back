package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.domain.Project;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionDetailRespDto {

    private Long projectId;
    private String projectName;
    private Integer currentWeek;
    private String status;
    private LocalDateTime submittedAt;

    public static SubmissionDetailRespDto from(Project project) {
        return SubmissionDetailRespDto.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .currentWeek(project.getCurrentWeek())
                .status(project.getStatus().name())
                .submittedAt(project.getSubmittedAt())
                .build();
    }
}
