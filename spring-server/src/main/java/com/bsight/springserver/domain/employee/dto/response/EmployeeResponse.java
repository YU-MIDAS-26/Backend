package com.bsight.springserver.domain.employee.dto.response;

import com.bsight.springserver.domain.employee.entity.Employee;
import com.bsight.springserver.domain.employee.entity.EmployeeStatus;
import com.bsight.springserver.domain.employee.entity.PayType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String name,
        LocalDate birthDate,
        String phoneNumber,
        String employeeNumber,
        PayType payType,
        Long payAmount,
        Boolean weeklyHolidayPayApplied,
        EmployeeStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getBirthDate(),
                employee.getPhoneNumber(),
                employee.getEmployeeNumber(),
                employee.getPayType(),
                employee.getPayAmount(),
                employee.getWeeklyHolidayPayApplied(),
                employee.getStatus(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}