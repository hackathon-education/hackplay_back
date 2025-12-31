package com.hackplay.hackplay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.MemberProgress;
import com.hackplay.hackplay.domain.Project;

@Repository
public interface MemberProgressRepository  extends JpaRepository<MemberProgress, Long>{
    void deleteByProject(Project project);
    Optional<MemberProgress> findByMemberAndProject(Member member, Project project);
}