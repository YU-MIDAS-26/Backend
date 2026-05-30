package com.bsight.springserver.domain.employee.dto.response;

public record EmployeeActionResponse(
        boolean success,
        String message
) {

    public static EmployeeActionResponse deleted() {
        return new EmployeeActionResponse(true, "직원이 삭제되었습니다.");
    }
}