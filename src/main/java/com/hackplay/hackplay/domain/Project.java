package com.hackplay.hackplay.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.hackplay.hackplay.common.CommonEnums;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private CommonEnums.Lecture lecture;

    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "total_week", nullable = false)
    private Integer totalWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Project(UUID uuid, String name, String description, String templateType, Boolean isPublic, Member member, CommonEnums.Lecture lecture) {
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
    }

    public void updateProjectInfo(String name, String description, Boolean isPublic) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAccessibleBy(String uuid) {
        return member != null
            && member.getUuid().equals(uuid);
    }
}
