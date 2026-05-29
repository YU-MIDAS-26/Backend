package com.bsight.springserver.domain.auth.dto.response;

import com.bsight.springserver.domain.user.entity.UserStatus;

public record RegisterStepTwoResponse(
        Long userId,
        Long businessProfileId,
        UserStatus status,
        String message
) {
    public static RegisterStepTwoResponse of(Long userId, Long businessProfileId, UserStatus status) {
        return new RegisterStepTwoResponse(
                userId,
                businessProfileId,
                status,
                "사업자 정보가 제출되었습니다. 관리자 승인 후 서비스 이용이 가능합니다."
        );
    }
}