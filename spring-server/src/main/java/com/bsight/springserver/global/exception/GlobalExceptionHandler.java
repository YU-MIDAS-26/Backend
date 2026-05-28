package com.bsight.springserver.global.exception;

import com.bsight.springserver.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 한 곳에서 처리하는 클래스
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 어노테이션 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation failed: {}", errorMessage);
        return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
    }

    /**
     * 비즈니스 로직 상의 커스텀 예외나 예상치 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception occurred: ", e);
        return ResponseEntity.internalServerError().body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}
