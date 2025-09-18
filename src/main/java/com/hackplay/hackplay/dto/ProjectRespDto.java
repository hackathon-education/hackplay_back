package com.hackplay.hackplay.dto;

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
    private String templateType;
    private Boolean isPublic;
    private String nickname;
    private String containerStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectRespDto from(Project project) {
        return ProjectRespDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .templateType(project.getTemplateType())
                .isPublic(project.getIsPublic())
                .nickname(project.getMember().getNickname())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public static ProjectRespDto from(Project project, String status) {
        ProjectRespDto dto = from(project);
        dto.containerStatus = status;
        return dto;
    }

}
