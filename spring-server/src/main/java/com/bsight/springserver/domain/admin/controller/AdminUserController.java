package com.bsight.springserver.domain.admin.controller;

import com.bsight.springserver.domain.admin.dto.request.AdminUserRejectRequest;
import com.bsight.springserver.domain.admin.dto.response.AdminPendingUserResponse;
import com.bsight.springserver.domain.admin.dto.response.AdminUserApprovalResponse;
import com.bsight.springserver.domain.admin.service.AdminUserService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "관리자 승인 대기 회원 목록 조회")
    @GetMapping("/pending")
    public ApiResponse<List<AdminPendingUserResponse>> getPendingApprovalUsers() {
        List<AdminPendingUserResponse> response = adminUserService.getPendingApprovalUsers();
        return ApiResponse.success("승인 대기 회원 목록 조회에 성공했습니다.", response);
    }

    @Operation(summary = "관리자 회원 승인")
    @PatchMapping("/{userId}/approve")
    public ApiResponse<AdminUserApprovalResponse> approveUser(
            @PathVariable Long userId
    ) {
        AdminUserApprovalResponse response = adminUserService.approveUser(userId);
        return ApiResponse.success("회원 승인이 완료되었습니다.", response);
    }

    @Operation(summary = "관리자 회원 가입 요청 거절")
    @PatchMapping("/{userId}/reject")
    public ApiResponse<AdminUserApprovalResponse> rejectUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserRejectRequest request
    ) {
        AdminUserApprovalResponse response = adminUserService.rejectUser(userId, request);
        return ApiResponse.success("회원 가입 요청이 거절되었습니다.", response);
    }
}