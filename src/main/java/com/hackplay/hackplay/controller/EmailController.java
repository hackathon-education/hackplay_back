package com.hackplay.hackplay.controller;

import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonResponse;
import com.hackplay.hackplay.dto.EmailAuthReqDto;
import com.hackplay.hackplay.dto.EmailVerifyReqDto;
import com.hackplay.hackplay.service.EmailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.FAIL_MAIL_SEND})
    @PostMapping("/send")
    public CommonResponse<Void> sendAuthEmail(@Valid @RequestBody EmailAuthReqDto emailAuthReqDto){
        emailService.sendEmail(emailAuthReqDto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.UNMATCHED_EMAIL_AUTH_CODE})
    @PostMapping("/verify")
    public CommonResponse<Void> verifyAuthEmail(@Valid @RequestBody EmailVerifyReqDto emailVerifyReqDto){
        emailService.verifyEmail(emailVerifyReqDto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.DUPLICATE_EMAIL})
    @PostMapping("/check")
    public CommonResponse<String> checkDuplicateEmail(@Valid @RequestBody EmailAuthReqDto emailAuthReqDto){
        return CommonResponse.success(emailService.checkDuplicateEmail(emailAuthReqDto));
    }
}
