package com.hackplay.hackplay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.common.CommonEnums.Lecture;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{

    boolean existsByNameAndMemberId(String name, Long id);

    Optional<Project> findByMemberAndLecture(Member member, Lecture lecture);

    boolean existsByMemberAndLecture(Member member, Lecture lecture);
    
}
