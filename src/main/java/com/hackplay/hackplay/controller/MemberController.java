package com.hackplay.hackplay.controller;

import java.io.IOException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.MemberReqDto;
import com.hackplay.hackplay.dto.MemberProfileRespDto;
import com.hackplay.hackplay.service.MemberService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 회원입니다.")
    @GetMapping
    public ApiResponse<MemberProfileRespDto> getMemberInfo(@AuthenticationPrincipal String uuid) {
        return ApiResponse.success(memberService.getMemberInfo(uuid));
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 회원입니다.")
    @PostMapping
    public ApiResponse<MemberProfileRespDto> updateMemberInfo(@AuthenticationPrincipal String uuid, MemberReqDto memberReqDto) {
        memberService.updateMemberInfo(uuid, memberReqDto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 파일 형식이거나 크기를 초과했습니다.")
    })
    @PostMapping("/profile-image")
    public ApiResponse<Void> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String uuid) throws IOException {
        memberService.uploadProfileImage(file, uuid);
        return ApiResponse.success();
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 회원입니다.")
    @DeleteMapping
    public ApiResponse<MemberProfileRespDto> deleteMemberInfo(@AuthenticationPrincipal String uuid) {
        memberService.deleteMemberInfo(uuid);
        return ApiResponse.success();
    }
}
