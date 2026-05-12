package com.hackplay.hackplay.controller;

import java.io.IOException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonResponse;
import com.hackplay.hackplay.dto.MemberProfileRespDto;
import com.hackplay.hackplay.dto.MemberReqDto;
import com.hackplay.hackplay.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @ApiErrorResponses({BaseResponseStatus.NO_EXIST_MEMBERS})
    @GetMapping
    public CommonResponse<MemberProfileRespDto> getMemberInfo(@AuthenticationPrincipal String uuid) {
        return CommonResponse.success(memberService.getMemberInfo(uuid));
    }

    @ApiErrorResponses({BaseResponseStatus.NO_EXIST_MEMBERS})
    @PostMapping
    public CommonResponse<Void> updateMemberInfo(@AuthenticationPrincipal String uuid, MemberReqDto memberReqDto) {
        memberService.updateMemberInfo(uuid, memberReqDto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.INVALID_FILE_TYPE, BaseResponseStatus.FILE_SIZE_EXCEEDED})
    @PostMapping("/profile-image")
    public CommonResponse<Void> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String uuid) throws IOException {
        memberService.uploadProfileImage(file, uuid);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.NO_EXIST_MEMBERS})
    @DeleteMapping
    public CommonResponse<Void> deleteMemberInfo(@AuthenticationPrincipal String uuid) {
        memberService.deleteMemberInfo(uuid);
        return CommonResponse.success();
    }
}
