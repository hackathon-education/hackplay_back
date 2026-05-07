package com.hackplay.hackplay.controller;

import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.EmailAuthReqDto;
import com.hackplay.hackplay.dto.EmailVerifyReqDto;
import com.hackplay.hackplay.service.EmailService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "메일 전송에 실패했습니다.")
    })
    @PostMapping("/send")
    public ApiResponse<Void> sendAuthEmail(@Valid @RequestBody EmailAuthReqDto emailAuthReqDto){
        emailService.sendEmail(emailAuthReqDto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증코드가 일치하지 않습니다.")
    })
    @PostMapping("/verify")
    public ApiResponse<Void> verifyAuthEmail(@Valid @RequestBody EmailVerifyReqDto emailVerifyReqDto){
        emailService.verifyEmail(emailVerifyReqDto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다.")
    })
    @PostMapping("/check")
    public ApiResponse<String> checkDuplicateEmail(@Valid @RequestBody EmailAuthReqDto emailAuthReqDto){
        return ApiResponse.success(emailService.checkDuplicateEmail(emailAuthReqDto));
    }
}
