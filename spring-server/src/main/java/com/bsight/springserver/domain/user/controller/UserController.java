package com.bsight.springserver.domain.user.controller;

import com.bsight.springserver.domain.user.dto.request.ChangePasswordRequest;
import com.bsight.springserver.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.bsight.springserver.domain.user.dto.response.UserActionResponse;
import com.bsight.springserver.domain.user.dto.response.UserMeResponse;
import com.bsight.springserver.domain.user.service.UserService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getMyInfo() {
        UserMeResponse response = userService.getMyInfo();

        return ApiResponse.success("내 정보 조회에 성공했습니다.", response);
    }

    @Operation(summary = "내 전화번호 변경")
    @PatchMapping("/me/phone")
    public ApiResponse<UserActionResponse> updatePhoneNumber(
            @Valid @RequestBody UpdatePhoneNumberRequest request
    ) {
        UserActionResponse response = userService.updatePhoneNumber(request);

        return ApiResponse.success("전화번호가 변경되었습니다.", response);
    }

    @Operation(summary = "내 비밀번호 변경")
    @PatchMapping("/me/password")
    public ApiResponse<UserActionResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        UserActionResponse response = userService.changePassword(request);

        return ApiResponse.success("비밀번호가 변경되었습니다.", response);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ApiResponse<UserActionResponse> deleteMe() {
        UserActionResponse response = userService.deleteMe();

        return ApiResponse.success("회원 탈퇴가 완료되었습니다.", response);
    }
}