package com.hackplay.hackplay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{

    boolean existsByEmail(String email); // 이메일 중복 여부
}