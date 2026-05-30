package com.bsight.springserver.domain.auth.dto.response;

public record AuthMessageResponse(
        boolean success,
        String message
) {
    public static AuthMessageResponse success(String message) {
        return new AuthMessageResponse(true, message);
    }
}