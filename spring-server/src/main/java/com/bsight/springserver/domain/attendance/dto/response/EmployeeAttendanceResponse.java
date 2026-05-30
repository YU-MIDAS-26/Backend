package com.bsight.springserver.domain.attendance.dto.response;

import com.bsight.springserver.domain.attendance.entity.AttendanceStatus;
import com.bsight.springserver.domain.attendance.entity.EmployeeAttendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EmployeeAttendanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate workDate,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        Boolean breakTimeApplied,
        LocalTime breakStartTime,
        LocalTime breakEndTime,
        AttendanceStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static EmployeeAttendanceResponse from(EmployeeAttendance attendance) {
        return new EmployeeAttendanceResponse(
                attendance.getId(),
                attendance.getEmployee().getId(),
                attendance.getEmployee().getName(),
                attendance.getWorkDate(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getBreakTimeApplied(),
                attendance.getBreakStartTime(),
                attendance.getBreakEndTime(),
                attendance.getStatus(),
                attendance.getCreatedAt(),
                attendance.getUpdatedAt()
        );
    }
}