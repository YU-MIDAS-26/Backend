package com.bsight.springserver.domain.payment.controller;

import com.bsight.springserver.domain.payment.dto.DailyStatsDto;
import com.bsight.springserver.domain.payment.dto.UploadResult;
import com.bsight.springserver.domain.payment.service.PaymentService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Payment", description = "판매전표 (결제 거래) API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "판매전표 CSV 업로드",
            description = "한 번에 여러 결제 거래를 CSV로 업로드한다. " +
                          "취소 거래·중복 거래·검증 실패 행은 스킵되고 응답에 사유가 기록된다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResult> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(
                "판매전표 업로드가 완료되었습니다.",
                paymentService.upload(file));
    }

    @Operation(summary = "일별 매출 추이",
            description = "기간 내 날짜별 매출 합계/건수. from/to 미입력 시 최근 30일.")
    @GetMapping("/stats/daily")
    public ApiResponse<List<DailyStatsDto>> dailyStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(paymentService.getDailyStats(from, to));
    }
}
