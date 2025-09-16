package com.hackplay.hackplay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.domain.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByProjectIdAndName(Long projectId, String name);
    
}
