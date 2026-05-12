package com.hackplay.hackplay.common;

import java.nio.file.AccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleValidationException(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult()
                           .getAllErrors()
                           .get(0)
                           .getDefaultMessage();
        BaseResponseStatus status = BaseResponseStatus.VALIDATION_ERROR;
        return ResponseEntity.status(status.getHttpStatus()).body(CommonResponse.fail(status.getHttpStatus().value(), message));
    }

    // PathVariable, RequestParam 예외 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<?>> handleConstraintViolationException(ConstraintViolationException ex){
        BaseResponseStatus status = BaseResponseStatus.VALIDATION_ERROR;
        return ResponseEntity.status(status.getHttpStatus()).body(CommonResponse.fail(status));
    }

    // 인증,인가 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<?>> handleAccessDenied(AccessDeniedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponse.fail(HttpStatus.FORBIDDEN.value(), null));
    }

    // BaseException 예외 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<CommonResponse<?>> handleBaseException(BaseException ex){
        BaseResponseStatus status = ex.getCode();
        return ResponseEntity.status(status.getHttpStatus()).body(CommonResponse.fail(status));
    }

    // 이외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleException(Exception ex){
        ex.printStackTrace();
        BaseResponseStatus status = BaseResponseStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status.getHttpStatus()).body(CommonResponse.fail(status));
    }
}
