package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.domain.Project;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionListRespDto {

    private Long projectId;
    private String projectName;
    private Integer currentWeek;
    private String status;
    private LocalDateTime submittedAt;

    public static SubmissionListRespDto from(Project project) {
        return SubmissionListRespDto.builder()
                .projectId(project.getId())
                .currentWeek(project.getCurrentWeek())
                .projectName(project.getName())
                .status(project.getStatus().toString())
                .submittedAt(project.getSubmittedAt())
                .build();
    }
}
