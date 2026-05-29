package com.bsight.springserver.domain.auth.dto.response;

public record PasswordResetResponse(
        boolean success,
        String message
) {

    public static PasswordResetResponse requested() {
        return new PasswordResetResponse(true, "비밀번호 재설정 링크를 이메일로 전송했습니다.");
    }

    public static PasswordResetResponse completed() {
        return new PasswordResetResponse(true, "비밀번호가 변경되었습니다.");
    }
}