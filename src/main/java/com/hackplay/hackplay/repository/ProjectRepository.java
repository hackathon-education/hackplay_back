package com.hackplay.hackplay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.domain.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{

    boolean existsByNameAndMemberId(String name, Long id);
    
}
