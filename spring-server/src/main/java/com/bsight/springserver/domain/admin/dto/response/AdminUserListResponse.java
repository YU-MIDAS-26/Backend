package com.bsight.springserver.domain.admin.dto.response;

import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserRole;

import java.time.LocalDateTime;

public record AdminUserListResponse(
        String userId,
        String name,
        LocalDateTime createdAt,
        UserRole role
) {

    public static AdminUserListResponse from(User user) {
        return new AdminUserListResponse(
                user.getStudentId(),
                user.getName(),
                user.getCreatedAt(),
                user.getRole()
        );
    }
}