package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.enums.lecture.Lecture;
import com.hackplay.hackplay.domain.Project;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectRespDto {
    private Long id;
    private String name;
    private String description;
    private Lecture lecture;
    private String templateType;
    private Boolean isPublic;
    private String nickname;
    private Integer week;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectRespDto from(Project project, Integer week) {
        return ProjectRespDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .lecture(project.getLecture())
                .templateType(project.getTemplateType())
                .isPublic(project.getIsPublic())
                .nickname(project.getMember().getNickname())
                .week(week)
                .isCompleted(project.isCompleted())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
