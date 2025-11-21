package com.hackplay.hackplay.dto;

import java.time.LocalDateTime;

import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.domain.Submission;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSubmissionListRespDto {

    private Long submissionId;
    private String email;
    private String nickname;
    private String lectureName; // 강의명
    private int week; // 주차 (1 ~ 4주차)
    private CommonEnums.SubmissionStatus status; // 채점 상태
    private LocalDateTime submittedAt; // 제출 시간

    public static AdminSubmissionListRespDto from(Submission submission) {
        return AdminSubmissionListRespDto.builder()
                .submissionId(submission.getId())
                .email(submission.getMember().getEmail())
                .nickname(submission.getMember().getNickname())
                .lectureName(submission.getProject().getLecture().getTitle())
                .week(submission.getWeek())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
