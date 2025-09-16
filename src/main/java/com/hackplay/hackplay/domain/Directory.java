package com.hackplay.hackplay.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Directory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Directory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId; // TODO: 프로젝트 도메인 생성 시 외래키 관계 수정 필요

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Directory(Long projectId,
                     Long parentId,
                     Member member,
                     String name,
                     String path,
                     LocalDateTime createdAt,
                     LocalDateTime updatedAt) {
        this.projectId = projectId;
        this.parentId = parentId;
        this.member = member;
        this.name = name;
        this.path = path;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 디렉토리 업데이트 시 사용 //
    public void updateDirNameAndPath(String newName, Long newParentId, String newPath) {
        this.name = newName;
        this.parentId = newParentId;
        this.path = newPath;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDirPath(String newPath) {
        this.path = newPath;
        this.updatedAt = LocalDateTime.now();
    }

}