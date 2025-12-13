package com.hackplay.hackplay.domain;

import java.time.LocalDateTime;

import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.common.CommonEnums.SubmissionStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "submission")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week", nullable = false)
    private int week = 1;

    @Column(name = "zip_path", nullable = false, length = 255)
    private String zipPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommonEnums.SubmissionStatus status = SubmissionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Builder
    public Submission(
            int week,
            String zipPath,
            Member member,
            Project project
    ) {
        this.week = week;
        this.zipPath = zipPath;
        this.member = member;
        this.project = project;
        this.status = CommonEnums.SubmissionStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
    }

    public void updateStatus(CommonEnums.SubmissionStatus status) {
        this.status = status;
    }
}
