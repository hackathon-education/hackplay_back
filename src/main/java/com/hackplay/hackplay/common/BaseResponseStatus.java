package com.hackplay.hackplay.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "DTO 유효성 검증에 실패했습니다."),

    // Auth ERROR
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    NO_EXIST_MEMBERS(HttpStatus.BAD_REQUEST, "존재하지 않는 회원정보입니다."),

    // INTERNAL SERVER ERROR 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류 발생"), 
    
    // Token ERROR
    TOKEN_EXPIRED(HttpStatus.INTERNAL_SERVER_ERROR,"토큰이 만료되었습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}