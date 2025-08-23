package com.hackplay.hackplay.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.config.BaseException;
import com.hackplay.hackplay.config.BaseResponseStatus;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.dto.SignupReqDto;
import com.hackplay.hackplay.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService{

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberRepository memberRepository;
    

    @Override
    @Transactional
    public void signup(SignupReqDto signupReqDto) {

        if(!signupReqDto.getPassword().equals(signupReqDto.getConfirmPassword()))
            throw new BaseException(BaseResponseStatus.PASSWORD_MISMATCH);

        if(memberRepository.existsByEmail(signupReqDto.getEmail()))
            throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL);

        Member member = Member.builder()
                        .uuid(UUID.randomUUID().toString())
                        .email(signupReqDto.getEmail())
                        .nickname(signupReqDto.getNickname())
                        .password(bCryptPasswordEncoder.encode(signupReqDto.getPassword()))
                        .role(signupReqDto.getRole())
                        .status(CommonEnums.Status.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        memberRepository.save(member);
    }
    
}
