package com.bsight.springserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentIdCheckRequest(

        @NotBlank(message = "아이디를 입력해 주세요.")
        @Size(min = 6, message = "아이디는 6자 이상이어야 합니다.")
        String studentId
) {
}