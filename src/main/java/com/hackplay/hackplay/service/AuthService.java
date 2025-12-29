package com.hackplay.hackplay.service;

import com.hackplay.hackplay.dto.SigninReqDto;
import com.hackplay.hackplay.dto.SigninResultRespDto;
import com.hackplay.hackplay.dto.SignupReqDto;

public interface AuthService {
    void signup(SignupReqDto signupReqDto);
    SigninResultRespDto signin(SigninReqDto signinReqDto);
    void signout();
}
