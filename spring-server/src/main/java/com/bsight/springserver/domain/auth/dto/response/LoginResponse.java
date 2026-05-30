package com.bsight.springserver.domain.auth.dto.response;

import com.bsight.springserver.domain.user.entity.User;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        LoginUserResponse user
) {

    public static LoginResponse of(String accessToken, Long expiresIn, User user) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresIn,
                LoginUserResponse.from(user)
        );
    }
}