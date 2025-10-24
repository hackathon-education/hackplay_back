package com.hackplay.hackplay.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackplay.hackplay.common.ApiResponse;
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
    
    @PostMapping("/send")
    public ApiResponse<Void> sendAuthEmail(@Valid @RequestBody EmailAuthReqDto emailAuthReqDto){
        emailService.sendEmail(emailAuthReqDto);
        return ApiResponse.success();
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verifyAuthEmail(@Valid @RequestBody EmailVerifyReqDto emailVerifyReqDto){
        emailService.verifyEmail(emailVerifyReqDto);
        return ApiResponse.success();
    }

    @PostMapping("/check")
    public ApiResponse<String> checkDuplicateEmail(@Valid @RequestBody EmailAuthReqDto emailAuthReqDto){
        return ApiResponse.success(emailService.checkDuplicateEmail(emailAuthReqDto));
    }
}
