package com.hackplay.hackplay.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "DTO 유효성 검증에 실패했습니다."),

    // ===================== Auth ERROR =====================
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    NO_EXIST_MEMBERS(HttpStatus.BAD_REQUEST, "존재하지 않는 회원정보입니다."),

    // ===================== Directory ERROR =====================
    ROOT_DIRECTORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "루트 디렉토리는 이미 존재합니다."),
    PARENT_DIRECTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "상위 디렉토리를 찾을 수 없습니다."),
    DUPLICATE_DIRECTORY_NAME(HttpStatus.BAD_REQUEST, "이미 존재하는 디렉토리명입니다."),
    DIRECTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 디렉토리를 찾을 수 없습니다."),
    CANNOT_DELETE_ROOT(HttpStatus.FORBIDDEN, "루트 디렉토리는 삭제할 수 없습니다."),

    // ===================== File ERROR =====================
    DUPLICATE_FILE_NAME(HttpStatus.BAD_REQUEST, "이미 존재하는 파일명입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
    FILE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 파일입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 1MB를 초과했습니다."),

    // ===================== Project ERROR =====================
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 프로젝트에 대한 접근 권한이 없습니다."),
    PROJECT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "프로젝트 생성에 실패했습니다."),
    PROJECT_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "프로젝트 삭제에 실패했습니다."),
    PROJECT_START_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "프로젝트 실행에 실패했습니다."),
    
    // ===================== Template ERROR =====================
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),

    // ===================== Script ERROR =====================
    SCRIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다."),

    // ===================== INTERNAL SERVER ERROR =====================
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류 발생"), 
    
    // ===================== Token ERROR =====================
    TOKEN_EXPIRED(HttpStatus.INTERNAL_SERVER_ERROR,"토큰이 만료되었습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}