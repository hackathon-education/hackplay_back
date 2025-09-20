package com.hackplay.hackplay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackplay.hackplay.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{

    boolean existsByEmail(String email); // 이메일 중복 여부

    Optional<Member> findByUuid(String uuid);

    Member findByEmail(String email); // 로그인 시 이메일로 회원 정보 조회
}