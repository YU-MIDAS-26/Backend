package com.bsight.springserver.domain.admin.dto.response;

import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;

public record AdminUserApprovalResponse(
        Long userId,
        String studentId,
        UserStatus status,
        String message
) {

    public static AdminUserApprovalResponse approved(User user) {
        return new AdminUserApprovalResponse(
                user.getId(),
                user.getStudentId(),
                user.getStatus(),
                "회원 승인이 완료되었습니다."
        );
    }

    public static AdminUserApprovalResponse rejected(User user) {
        return new AdminUserApprovalResponse(
                user.getId(),
                user.getStudentId(),
                user.getStatus(),
                "회원 가입 요청이 거절되었습니다."
        );
    }
}