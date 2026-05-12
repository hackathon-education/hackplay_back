package com.hackplay.hackplay.common;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonResponse<T> {
    private int code;       // 응답 코드
    private String message; // 응답 메시지
    private T data;         // 데이터

    public CommonResponse(int code, String message){
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public CommonResponse(BaseResponseStatus status){
        this.code = status.getHttpStatus().value();
        this.message = status.getMessage();
        this.data = null;
    }

    // 성공 시 -> 200, 성공 메시지
    public static <T> CommonResponse<T> success(){
        return new CommonResponse<>(HttpStatus.OK.value(), "요청에 성공하였습니다.");
    }

    // 성공 시 -> 200, 성공 메시지, data
    public static <T> CommonResponse<T> success(T data){
        return new CommonResponse<>(HttpStatus.OK.value(), "요청에 성공하였습니다.", data);
    }

    // 실패 시 -> 오류 코드, 오류 메시지
    public static <T> CommonResponse<T> fail(BaseResponseStatus status){
        return new CommonResponse<>(status.getHttpStatus().value(), status.getMessage());
    }

    public static <T> CommonResponse<T> fail(int code, String message){
        return new CommonResponse<>(code, message);
    }
}