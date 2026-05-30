package com.bsight.springserver.domain.auth.dto.response;

import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserRole;
import com.bsight.springserver.domain.user.entity.UserStatus;

public record LoginUserResponse(
        Long id,
        String studentId,
        String name,
        String email,
        UserRole role,
        UserStatus status
) {

    public static LoginUserResponse from(User user) {
        return new LoginUserResponse(
                user.getId(),
                user.getStudentId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
}