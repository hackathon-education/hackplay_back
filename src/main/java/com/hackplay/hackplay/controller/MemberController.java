package com.hackplay.hackplay.controller;

import java.io.IOException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.MemberReqDto;
import com.hackplay.hackplay.dto.MemberProfileRespDto;
import com.hackplay.hackplay.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    
    // 현재 사용자 정보 조회
    @GetMapping
    public ApiResponse<MemberProfileRespDto> getMemberInfo(@AuthenticationPrincipal String uuid) {
        return ApiResponse.success(memberService.getMemberInfo(uuid));
    }

    // 현재 사용자 정보 수정
    @PostMapping
    public ApiResponse<MemberProfileRespDto> updateMemberInfo(@AuthenticationPrincipal String uuid, MemberReqDto memberReqDto) {
        memberService.updateMemberInfo(uuid, memberReqDto);
        return ApiResponse.success();
    }

    // 프로필 이미지 업로드
    @PostMapping("/profile-image")
    public ApiResponse<Void> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String uuid) throws IOException {
        memberService.uploadProfileImage(file, uuid);
        return ApiResponse.success();
    }

    // 회원 탈퇴
    @DeleteMapping
    public ApiResponse<MemberProfileRespDto> deleteMemberInfo(@AuthenticationPrincipal String uuid) {
        memberService.deleteMemberInfo(uuid);
        return ApiResponse.success();
    }

}
