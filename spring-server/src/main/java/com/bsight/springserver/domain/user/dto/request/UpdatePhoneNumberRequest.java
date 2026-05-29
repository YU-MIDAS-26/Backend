package com.bsight.springserver.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePhoneNumberRequest(

        @NotBlank(message = "전화번호를 입력해 주세요.")
        @Pattern(regexp = "^[0-9]{11}$", message = "전화번호는 하이픈 없이 11자리로 입력해 주세요.")
        String phoneNumber
) {
}