package com.hackplay.hackplay.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonResponse;
import com.hackplay.hackplay.dto.*;
import com.hackplay.hackplay.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.EMAIL_NOT_VERIFIED, BaseResponseStatus.DUPLICATE_EMAIL})
    @PostMapping("/signup")
    public CommonResponse<Void> signup(@Valid @RequestBody SignupReqDto signupReqDto){
        authService.signup(signupReqDto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.INVALID_LOGIN})
    @PostMapping("/signin")
    public CommonResponse<SigninRespDto> signin(@Valid @RequestBody SigninReqDto signinReqDto, HttpServletResponse response){
        SigninResultRespDto signinResultRespDto = authService.signin(signinReqDto);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", signinResultRespDto.getAccessToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", signinResultRespDto.getRefreshToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60 * 24 * 7).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return CommonResponse.success(signinResultRespDto.getSigninRespDto());
    }

    @PostMapping("/signout")
    public CommonResponse<Void> signout(@AuthenticationPrincipal String uuid, HttpServletResponse response){
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
            .path("/").httpOnly(true).secure(true).sameSite("None").maxAge(0).build();
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
            .path("/").httpOnly(true).secure(true).sameSite("None").maxAge(0).build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        authService.signout(uuid);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.INVALID_TOKEN})
    @PostMapping("/reissue")
    public CommonResponse<Void> reissue(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        ReissueRespDto reissueRespDto = authService.reissue(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", reissueRespDto.getAccessToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", reissueRespDto.getRefreshToken())
            .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(60 * 60 * 24 * 7).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return CommonResponse.success();
    }
}
