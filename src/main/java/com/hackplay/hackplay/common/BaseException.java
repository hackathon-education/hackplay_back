package com.hackplay.hackplay.config;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final BaseResponseStatus code;

    public BaseException(BaseResponseStatus code){
        super(code.getMessage());
        this.code = code;
    }

    public static void sendErrorResponse(HttpServletResponse response, BaseResponseStatus status) throws IOException{
        response.setStatus(status.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> errorResponse = ApiResponse.fail(status);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}