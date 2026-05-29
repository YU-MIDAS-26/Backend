package com.bsight.springserver.domain.auth.dto.response;

public record LogoutResponse(
        boolean success,
        String message
) {

    public static LogoutResponse success() {
        return new LogoutResponse(true, "로그아웃이 완료되었습니다.");
    }
}