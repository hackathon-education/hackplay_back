package com.hackplay.hackplay.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.config.jwt.TokenProvider;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.dto.SigninReqDto;
import com.hackplay.hackplay.dto.SigninRespDto;
import com.hackplay.hackplay.dto.SignupReqDto;
import com.hackplay.hackplay.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService{

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    

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
    
    @Override
    @Transactional
    public SigninRespDto signin(SigninReqDto signinReqDto){

        // 회원 존재 X
        Member member = memberRepository.findByEmail(signinReqDto.getEmail());
        if (member == null || !bCryptPasswordEncoder.matches(signinReqDto.getPassword(),
            member.getPassword())) {
            throw new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS);
        }

        String accessToken = tokenProvider.createAccessToken(member.getUuid());
        String refreshToken = tokenProvider.createRefreshToken(member.getUuid());

        // 회원 리프레쉬 토큰 및 마지막 로그인 시점 DB 저장.
        member.signinUpdate(refreshToken);

        SigninRespDto signinRespDto = SigninRespDto.entityToDto(member, accessToken, refreshToken);

        return signinRespDto;
    }
}
