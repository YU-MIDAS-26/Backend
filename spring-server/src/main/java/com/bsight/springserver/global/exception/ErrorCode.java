package com.bsight.springserver.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "토큰이 필요합니다."),
    ALREADY_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    LOGIN_NOT_ALLOWED(HttpStatus.FORBIDDEN, "관리자 승인 완료 후 로그인이 가능합니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_STEP_ONE_NOT_FOUND(HttpStatus.NOT_FOUND, "회원가입 1단계 정보를 찾을 수 없습니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "현재 회원 상태에서는 요청을 처리할 수 없습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    STUDENT_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    PASSWORD_CONFIRM_NOT_MATCHED(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "약관 동의를 완료해 주세요."),

    PASSWORD_RESET_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "아이디와 이메일이 일치하는 회원을 찾을 수 없습니다."),
    PASSWORD_RESET_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "비밀번호 재설정 요청 내역이 없습니다."),
    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "비밀번호 재설정 링크가 만료되었습니다."),
    PASSWORD_RESET_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 비밀번호 재설정 링크입니다."),

    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "이메일 인증 요청 내역이 없습니다."),
    INVALID_EMAIL_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증을 완료해 주세요."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 발송에 실패했습니다."),

    BUSINESS_PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사업자 정보가 등록된 회원입니다."),
    BUSINESS_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "사업자 정보를 찾을 수 없습니다."),
    BUSINESS_REGISTRATION_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 사업자 등록 번호입니다."),

    FILE_REQUIRED(HttpStatus.BAD_REQUEST, "첨부 파일이 필요합니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다. pdf, jpg, jpeg, png 파일만 업로드할 수 있습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다."),

    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."),
    EMPLOYEE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 사원번호입니다."),

    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "근무 기록을 찾을 수 없습니다."),
    ATTENDANCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 날짜에 이미 등록된 직원 근무 기록이 있습니다."),
    INVALID_WORK_TIME(HttpStatus.BAD_REQUEST, "퇴근 시간은 출근 시간보다 늦어야 합니다."),
    INVALID_BREAK_TIME(HttpStatus.BAD_REQUEST, "휴게시간은 출근 시간과 퇴근 시간 사이여야 합니다."),
    BREAK_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "휴게시간 적용 시 시작 시간과 종료 시간을 모두 입력해야 합니다."),

    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "재료를 찾을 수 없습니다."),
    INGREDIENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 재료입니다."),

    PRICE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "가격 이력을 찾을 수 없습니다."),

    NAVER_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "네이버 쇼핑 API 호출에 실패했습니다."),
    CRAWLING_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "가격 정보 수집에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}