package com.bsight.springserver.domain.user.dto.response;

public record UserActionResponse(
        boolean success,
        String message
) {

    public static UserActionResponse phoneUpdated() {
        return new UserActionResponse(true, "전화번호가 변경되었습니다.");
    }

    public static UserActionResponse passwordChanged() {
        return new UserActionResponse(true, "비밀번호가 변경되었습니다.");
    }

    public static UserActionResponse deleted() {
        return new UserActionResponse(true, "회원 탈퇴가 완료되었습니다.");
    }
}