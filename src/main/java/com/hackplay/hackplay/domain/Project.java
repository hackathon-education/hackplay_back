package com.hackplay.hackplay.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.hackplay.hackplay.common.enums.lecture.Lecture;
import com.hackplay.hackplay.common.enums.submission.SubmissionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "project",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "lecture"})
    }
)
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false, length = 36)
    private String uuid;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "lecture", nullable = false)
    private Lecture lecture;

    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;

    @Column(name = "zip_path", nullable = true, length = 255)
    private String zipPath;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "current_week", nullable = false)
    private int currentWeek = 1;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(name = "total_week", nullable = false)
    private Integer totalWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status = SubmissionStatus.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at", nullable = true)
    private LocalDateTime submittedAt;

    @Builder
    public Project(UUID uuid, String name, String description, String templateType, Boolean isPublic, Member member, Lecture lecture) {
        this.uuid = uuid.toString();
        this.name = name;
        this.description = description;
        this.lecture = lecture;
        this.templateType = templateType;
        this.isPublic = isPublic;
        this.totalWeek = lecture.getTotalWeek();
        this.member = member;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentWeek = 1;
        this.isCompleted = false;
        this.status = SubmissionStatus.NONE;
    }

    public void updateProjectInfo(String name, String description, Boolean isPublic) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }

    // 다음 주차 Unlock
    public void unlockNextWeek() {
        if (this.currentWeek < this.totalWeek) {
            this.currentWeek += 1;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    // 해당 프로젝트 완료
    public void complete() {
        this.isCompleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ===================================
    // 제출 정보 업데이트
    // ===================================
    public void updateSubmission(String zipPath, SubmissionStatus status) {
        this.zipPath = zipPath;
        this.status = status;
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSubmissionStatus(SubmissionStatus status) {
        if (!this.status.isFinal()) {
            this.status = status;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // ===================================
    // 상태 확인
    // ===================================
    public boolean isAccessibleBy(String uuid) {
        return member != null && member.getUuid().equals(uuid);
    }

    public boolean canSubmit() {
        return !this.status.isFinal() && this.currentWeek <= this.totalWeek;
    }

    public boolean isSubmissionLocked() {
        return this.status.isFinal();
    }

    public double getProgressPercentage() {
        return (double) this.currentWeek / this.totalWeek * 100;
    }

    public boolean isAllCompleted() {
        return this.currentWeek >= this.totalWeek && this.isCompleted;
    }



}
