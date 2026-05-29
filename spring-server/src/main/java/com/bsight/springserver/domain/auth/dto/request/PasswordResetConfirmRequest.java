package com.bsight.springserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(

        @NotBlank(message = "비밀번호 재설정 토큰이 필요합니다.")
        String token,

        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$",
                message = "비밀번호는 대소문자를 포함해 6자 이상이어야 합니다."
        )
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해 주세요.")
        String newPasswordConfirm
) {
}