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
@Table(name = "file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // TODO: 추후 실제 프로젝트 테이블, 디렉토리 테이블과 연관관계 매핑 필요
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "dir_id", nullable = false)
    private Long dirId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public File(Long projectId,
                Long dirId,
                Member member,
                String name,
                String path,
                Long size,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.projectId = projectId;
        this.dirId = dirId;
        this.member = member;
        this.name = name;
        this.path = path;
        this.size = size;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateFile(String newContentPath, Long newSize) {
        this.path = newContentPath;
        this.size = newSize;
        this.updatedAt = LocalDateTime.now();
    }
}
