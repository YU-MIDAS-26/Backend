package com.bsight.springserver.domain.attendance.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record EmployeeAttendanceUpdateRequest(

        @NotNull(message = "직원 ID를 입력해 주세요.")
        Long employeeId,

        @NotNull(message = "근무 날짜를 입력해 주세요.")
        LocalDate workDate,

        @NotNull(message = "출근 시간을 입력해 주세요.")
        LocalTime checkInTime,

        @NotNull(message = "퇴근 시간을 입력해 주세요.")
        LocalTime checkOutTime,

        @NotNull(message = "휴게시간 적용 여부를 선택해 주세요.")
        Boolean breakTimeApplied,

        LocalTime breakStartTime,

        LocalTime breakEndTime
) {
}