package com.bsight.springserver.domain.auth.controller;

import com.bsight.springserver.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.bsight.springserver.domain.auth.dto.request.EmailVerificationRequest;
import com.bsight.springserver.domain.auth.dto.request.LoginRequest;
import com.bsight.springserver.domain.auth.dto.request.RegisterStepOneRequest;
import com.bsight.springserver.domain.auth.dto.request.RegisterStepTwoRequest;
import com.bsight.springserver.domain.auth.dto.request.StudentIdCheckRequest;
import com.bsight.springserver.domain.auth.dto.response.AuthMessageResponse;
import com.bsight.springserver.domain.auth.dto.response.LoginResponse;
import com.bsight.springserver.domain.auth.dto.response.RegisterStepTwoResponse;
import com.bsight.springserver.domain.auth.service.AuthService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 인증코드 요청")
    @PostMapping("/email/request")
    public AuthMessageResponse requestEmailVerificationCode(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        authService.requestEmailVerificationCode(request);
        return AuthMessageResponse.success("인증코드를 이메일로 전송했습니다.");
    }

    @Operation(summary = "이메일 인증번호 확인")
    @PostMapping("/email/verify")
    public AuthMessageResponse verifyEmailVerificationCode(
            @Valid @RequestBody EmailVerificationConfirmRequest request
    ) {
        authService.verifyEmailVerificationCode(request);
        return AuthMessageResponse.success("이메일 인증이 완료되었습니다.");
    }

    @Operation(summary = "아이디 중복 확인")
    @PostMapping("/student-id/check")
    public AuthMessageResponse checkStudentIdDuplicate(
            @Valid @RequestBody StudentIdCheckRequest request
    ) {
        authService.checkStudentIdDuplicate(request);
        return AuthMessageResponse.success("사용 가능한 아이디입니다.");
    }

    @Operation(summary = "회원가입 1단계 기본정보 저장")
    @PostMapping("/register/step-one")
    public AuthMessageResponse registerStepOne(
            @Valid @RequestBody RegisterStepOneRequest request
    ) {
        authService.registerStepOne(request);
        return AuthMessageResponse.success("1단계 정보가 저장되었습니다.");
    }

    @Operation(summary = "회원가입 2단계 사업자 정보 저장")
    @PostMapping(
            value = "/register/step-two",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<RegisterStepTwoResponse> registerStepTwo(
            @Valid @ModelAttribute RegisterStepTwoRequest request
    ) {
        RegisterStepTwoResponse response = authService.registerStepTwo(request);
        return ApiResponse.success("사업자 정보가 제출되었습니다.", response);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("로그인에 성공했습니다.", response);
    }
}