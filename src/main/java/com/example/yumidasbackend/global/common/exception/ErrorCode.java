package com.example.yumidasbackend.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // 재료
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "재료를 찾을 수 없습니다."),
    INGREDIENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 재료입니다."),

    // 가격
    PRICE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "가격 이력을 찾을 수 없습니다."),

    // 외부 API
    NAVER_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "네이버 쇼핑 API 호출에 실패했습니다."),
    CRAWLING_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "가격 정보 수집에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
