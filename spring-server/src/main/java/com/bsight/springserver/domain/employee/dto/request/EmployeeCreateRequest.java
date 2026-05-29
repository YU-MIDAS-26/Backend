package com.bsight.springserver.domain.employee.dto.request;

import com.bsight.springserver.domain.employee.entity.PayType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EmployeeCreateRequest(

        @NotBlank(message = "이름을 입력해 주세요.")
        String name,

        @NotNull(message = "생년월일을 입력해 주세요.")
        LocalDate birthDate,

        @NotBlank(message = "전화번호를 입력해 주세요.")
        @Pattern(regexp = "^[0-9]{11}$", message = "전화번호는 하이픈 없이 11자리로 입력해 주세요.")
        String phoneNumber,

        @Size(max = 50, message = "사원번호는 50자 이하로 입력해 주세요.")
        String employeeNumber,

        @NotNull(message = "급여 방식을 선택해 주세요.")
        PayType payType,

        @NotNull(message = "급여 금액을 입력해 주세요.")
        @Positive(message = "급여 금액은 0보다 커야 합니다.")
        Long payAmount,

        @NotNull(message = "주휴수당 적용 여부를 선택해 주세요.")
        Boolean weeklyHolidayPayApplied
) {
}