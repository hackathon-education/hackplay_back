package com.hackplay.hackplay.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.ReissueRespDto;
import com.hackplay.hackplay.dto.SigninReqDto;
import com.hackplay.hackplay.dto.SigninRespDto;
import com.hackplay.hackplay.dto.SigninResultRespDto;
import com.hackplay.hackplay.dto.SignupReqDto;
import com.hackplay.hackplay.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
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

    @PostMapping("/signin")
    public ApiResponse<SigninRespDto> signin(@Valid @RequestBody SigninReqDto signinReqDto, HttpServletResponse response){
        SigninResultRespDto signinResultRespDto = authService.signin(signinReqDto);    

        ResponseCookie accessCookie = ResponseCookie.from(
                "accessToken",
                signinResultRespDto.getAccessToken()
        )
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(60 * 60)
            .build();

        ResponseCookie refreshCookie = ResponseCookie.from(
                "refreshToken",
                signinResultRespDto.getRefreshToken()
        )
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(60 * 60 * 24 * 7)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        
        return ApiResponse.success(signinResultRespDto.getSigninRespDto());
    }

    @PostMapping("/signout")
    public ApiResponse<Void> signout(@AuthenticationPrincipal String uuid, HttpServletResponse response){
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
            .path("/")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .maxAge(0)
            .build();

        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
            .path("/")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .maxAge(0)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        authService.signout(uuid);
        return ApiResponse.success();
    }

    @PostMapping("/reissue")
    public ApiResponse<Void> reissue(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        ReissueRespDto reissueRespDto = authService.reissue(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from(
                "accessToken",
                reissueRespDto.getAccessToken()
        )
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(60 * 60)
            .build();

        ResponseCookie refreshCookie = ResponseCookie.from(
                "refreshToken",
                reissueRespDto.getRefreshToken()
        )
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(60 * 60 * 24 * 7)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.success();
    }

}
