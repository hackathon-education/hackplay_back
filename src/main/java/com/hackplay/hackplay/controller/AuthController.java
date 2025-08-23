package com.hackplay.hackplay.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackplay.hackplay.config.ApiResponse;
import com.hackplay.hackplay.dto.SignupReqDto;
import com.hackplay.hackplay.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupReqDto signupReqDto){
        authService.signup(signupReqDto);
        return ApiResponse.success();
    }
}