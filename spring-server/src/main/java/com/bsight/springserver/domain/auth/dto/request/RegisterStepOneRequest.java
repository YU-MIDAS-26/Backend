package com.bsight.springserver.domain.auth.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterStepOneRequest(

        @NotBlank(message = "이름을 입력해 주세요.")
        String name,

        @NotNull(message = "생년월일을 입력해 주세요.")
        LocalDate birthDate,

        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "이메일 인증번호를 입력해 주세요.")
        @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String emailVerificationCode,

        @NotBlank(message = "전화번호를 입력해 주세요.")
        @Pattern(regexp = "^[0-9]{11}$", message = "전화번호는 하이픈 없이 11자리로 입력해 주세요.")
        String phoneNumber,

        @NotBlank(message = "아이디를 입력해 주세요.")
        @Size(min = 6, message = "아이디는 6자 이상이어야 합니다.")
        String studentId,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$",
                message = "비밀번호는 대소문자를 포함해 6자 이상이어야 합니다."
        )
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
        String passwordConfirm,

        @AssertTrue(message = "약관동의1을 완료해 주세요.")
        Boolean agreedToTerms1,

        @AssertTrue(message = "약관동의2를 완료해 주세요.")
        Boolean agreedToTerms2
) {
}