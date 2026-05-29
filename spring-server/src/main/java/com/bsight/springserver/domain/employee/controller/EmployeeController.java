package com.bsight.springserver.domain.employee.controller;

import com.bsight.springserver.domain.employee.dto.request.EmployeeCreateRequest;
import com.bsight.springserver.domain.employee.dto.request.EmployeeUpdateRequest;
import com.bsight.springserver.domain.employee.dto.response.EmployeeActionResponse;
import com.bsight.springserver.domain.employee.dto.response.EmployeeResponse;
import com.bsight.springserver.domain.employee.service.EmployeeService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "직원 등록")
    @PostMapping
    public ApiResponse<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request
    ) {
        EmployeeResponse response = employeeService.createEmployee(request);

        return ApiResponse.success("직원이 등록되었습니다.", response);
    }

    @Operation(summary = "직원 목록 조회")
    @GetMapping
    public ApiResponse<List<EmployeeResponse>> getEmployees() {
        List<EmployeeResponse> response = employeeService.getEmployees();

        return ApiResponse.success("직원 목록 조회에 성공했습니다.", response);
    }

    @Operation(summary = "직원 상세 조회")
    @GetMapping("/{employeeId}")
    public ApiResponse<EmployeeResponse> getEmployee(
            @PathVariable Long employeeId
    ) {
        EmployeeResponse response = employeeService.getEmployee(employeeId);

        return ApiResponse.success("직원 상세 조회에 성공했습니다.", response);
    }

    @Operation(summary = "직원 정보 수정")
    @PatchMapping("/{employeeId}")
    public ApiResponse<EmployeeResponse> updateEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        EmployeeResponse response = employeeService.updateEmployee(employeeId, request);

        return ApiResponse.success("직원 정보가 수정되었습니다.", response);
    }

    @Operation(summary = "직원 삭제")
    @DeleteMapping("/{employeeId}")
    public ApiResponse<EmployeeActionResponse> deleteEmployee(
            @PathVariable Long employeeId
    ) {
        EmployeeActionResponse response = employeeService.deleteEmployee(employeeId);

        return ApiResponse.success("직원이 삭제되었습니다.", response);
    }
}