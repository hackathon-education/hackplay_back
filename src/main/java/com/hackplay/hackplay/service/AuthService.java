package com.hackplay.hackplay.service;

import com.hackplay.hackplay.dto.SigninReqDto;
import com.hackplay.hackplay.dto.SigninRespDto;
import com.hackplay.hackplay.dto.SignupReqDto;

public interface AuthService {
    void signup(SignupReqDto signupReqDto);
    SigninRespDto signin(SigninReqDto signinReqDto);
    void signout();
}
