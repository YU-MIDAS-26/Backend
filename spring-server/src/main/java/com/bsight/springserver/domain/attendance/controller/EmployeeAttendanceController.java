package com.bsight.springserver.domain.attendance.controller;

import com.bsight.springserver.domain.attendance.dto.request.EmployeeAttendanceCreateRequest;
import com.bsight.springserver.domain.attendance.dto.request.EmployeeAttendanceUpdateRequest;
import com.bsight.springserver.domain.attendance.dto.response.EmployeeAttendanceActionResponse;
import com.bsight.springserver.domain.attendance.dto.response.EmployeeAttendanceResponse;
import com.bsight.springserver.domain.attendance.service.EmployeeAttendanceService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee-attendances")
@RequiredArgsConstructor
public class EmployeeAttendanceController {

    private final EmployeeAttendanceService employeeAttendanceService;

    @Operation(summary = "직원 근무시간 등록")
    @PostMapping
    public ApiResponse<EmployeeAttendanceResponse> createAttendance(
            @Valid @RequestBody EmployeeAttendanceCreateRequest request
    ) {
        EmployeeAttendanceResponse response = employeeAttendanceService.createAttendance(request);

        return ApiResponse.success("직원 근무시간이 등록되었습니다.", response);
    }

    @Operation(summary = "날짜별 직원 근무 목록 조회")
    @GetMapping
    public ApiResponse<List<EmployeeAttendanceResponse>> getAttendancesByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate workDate
    ) {
        List<EmployeeAttendanceResponse> response = employeeAttendanceService.getAttendancesByDate(workDate);

        return ApiResponse.success("날짜별 직원 근무 목록 조회에 성공했습니다.", response);
    }

    @Operation(summary = "직원 근무시간 상세 조회")
    @GetMapping("/{attendanceId}")
    public ApiResponse<EmployeeAttendanceResponse> getAttendance(
            @PathVariable Long attendanceId
    ) {
        EmployeeAttendanceResponse response = employeeAttendanceService.getAttendance(attendanceId);

        return ApiResponse.success("직원 근무시간 상세 조회에 성공했습니다.", response);
    }

    @Operation(summary = "직원 근무시간 수정")
    @PatchMapping("/{attendanceId}")
    public ApiResponse<EmployeeAttendanceResponse> updateAttendance(
            @PathVariable Long attendanceId,
            @Valid @RequestBody EmployeeAttendanceUpdateRequest request
    ) {
        EmployeeAttendanceResponse response = employeeAttendanceService.updateAttendance(attendanceId, request);

        return ApiResponse.success("직원 근무시간이 수정되었습니다.", response);
    }

    @Operation(summary = "직원 근무시간 삭제")
    @DeleteMapping("/{attendanceId}")
    public ApiResponse<EmployeeAttendanceActionResponse> deleteAttendance(
            @PathVariable Long attendanceId
    ) {
        EmployeeAttendanceActionResponse response = employeeAttendanceService.deleteAttendance(attendanceId);

        return ApiResponse.success("직원 근무시간이 삭제되었습니다.", response);
    }
}