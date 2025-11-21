package com.hackplay.hackplay.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "member_progress")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_week", nullable = false)
    private int currentWeek = 1;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MemberProgress(
            Member member,
            Project project
    ) {
        this.member = member;
        this.project = project;
        this.updatedAt = LocalDateTime.now();
    }

    // 다음 주차 Unlock
    public void unlockNextWeek() {
        this.currentWeek += 1;
        this.updatedAt = LocalDateTime.now();
    }

    // 해당 프로젝트 완료
    public void complete() {
        this.isCompleted = true;
        this.updatedAt = LocalDateTime.now();
    }
}
