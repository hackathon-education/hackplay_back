package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.common.enums.submission.SubmissionStatus;
import com.hackplay.hackplay.domain.Project;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubmissionListRespDto {

    private Long projectId;
    private String email;
    private String nickname;
    private String lectureName; // 강의명
    private int currnetWeek; // 현재 주차
    private SubmissionStatus status; // 채점 상태
    private LocalDateTime submittedAt; // 제출 시간

    public static AdminSubmissionListRespDto from(Project project) {
        return AdminSubmissionListRespDto.builder()
                .projectId(project.getId())
                .email(project.getMember().getEmail())
                .nickname(project.getMember().getNickname())
                .lectureName(project.getLecture().getTitle())
                .currnetWeek(project.getCurrentWeek())
                .status(project.getStatus())
                .submittedAt(project.getSubmittedAt())
                .build();
    }
}
