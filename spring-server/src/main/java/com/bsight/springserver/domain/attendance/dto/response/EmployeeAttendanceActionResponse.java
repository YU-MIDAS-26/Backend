package com.bsight.springserver.domain.attendance.dto.response;

public record EmployeeAttendanceActionResponse(
        boolean success,
        String message
) {

    public static EmployeeAttendanceActionResponse deleted() {
        return new EmployeeAttendanceActionResponse(true, "근무 기록이 삭제되었습니다.");
    }
}