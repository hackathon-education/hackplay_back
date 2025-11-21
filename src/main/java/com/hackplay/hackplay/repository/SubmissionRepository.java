package com.hackplay.hackplay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.Submission;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long>{

    List<Submission> findAllByMember(Member member);
    
}
