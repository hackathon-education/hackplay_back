package com.hackplay.hackplay.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.*;
import com.hackplay.hackplay.service.AuthService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이메일 인증이 완료되지 않았습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다.")
    })
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupReqDto signupReqDto){
        authService.signup(signupReqDto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않습니다.")
    })
    @PostMapping("/signin")
    public ApiResponse<SigninRespDto> signin(@Valid @RequestBody SigninReqDto signinReqDto, HttpServletResponse response){
        SigninResultRespDto signinResultRespDto = authService.signin(signinReqDto);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", signinResultRespDto.getAccessToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", signinResultRespDto.getRefreshToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60 * 24 * 7).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.success(signinResultRespDto.getSigninRespDto());
    }

    @PostMapping("/signout")
    public ApiResponse<Void> signout(@AuthenticationPrincipal String uuid, HttpServletResponse response){
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
            .path("/").httpOnly(true).secure(true).sameSite("None").maxAge(0).build();
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
            .path("/").httpOnly(true).secure(true).sameSite("None").maxAge(0).build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        authService.signout(uuid);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰입니다.")
    })
    @PostMapping("/reissue")
    public ApiResponse<Void> reissue(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        ReissueRespDto reissueRespDto = authService.reissue(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", reissueRespDto.getAccessToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", reissueRespDto.getRefreshToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60 * 24 * 7).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.success();
    }
}
