package com.hackplay.hackplay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.domain.Directory;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long>{

    // 루트 디렉토리 중복 체크
    boolean existsByProjectIdAndParentIdIsNull(Long projectId);
    // 동일 부모 아래 중복 이름 방지
    boolean existsByProjectIdAndParentIdAndName(Long projectId, Long parentId, String name);
    Optional<Directory> findByIdAndProjectId(Long dirId, Long projectId);
    List<Directory> findByParentId(Long parentId);
    Optional<Directory> findByProjectIdAndParentIdIsNull(Long id);
}
