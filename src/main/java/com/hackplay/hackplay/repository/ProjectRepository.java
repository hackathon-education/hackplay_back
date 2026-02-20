package com.hackplay.hackplay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.common.enums.lecture.Lecture;
import com.hackplay.hackplay.common.enums.submission.SubmissionStatus;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{

    List<Project> findByMember(Member member);

    List<Project> findByStatus(SubmissionStatus status);

    List<Project> findByMemberAndStatus(Member member, SubmissionStatus status);

    boolean existsByNameAndMemberId(String name, Long id);

    Optional<Project> findByMemberAndLecture(Member member, Lecture lecture);

    boolean existsByMemberAndLecture(Member member, Lecture lecture);

    @Query("""
        select p
        from Project p
        join fetch p.member
        where p.id = :id
    """)
    Optional<Project> findByIdWithMember(@Param("id") Long id);
}
