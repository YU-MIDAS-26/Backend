package com.bsight.springserver.domain.user.dto.response;

import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserRole;
import com.bsight.springserver.domain.user.entity.UserStatus;

import java.time.LocalDate;

public record UserMeResponse(
        Long id,
        String studentId,
        String name,
        String email,
        LocalDate birthDate,
        String phoneNumber,
        UserRole role,
        UserStatus status
) {

    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getStudentId(),
                user.getName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus()
        );
    }
}