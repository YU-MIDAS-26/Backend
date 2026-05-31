package com.bsight.springserver.domain.sales.controller;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.sales.dto.request.SalesCreateRequest;
import com.bsight.springserver.domain.sales.dto.response.SalesPeriodResponse;
import com.bsight.springserver.domain.sales.service.SalesService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Sales", description = "매출 관리 API")
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @Operation(summary = "매출 등록", description = "일별/주별/월별/시간별 매출 데이터를 등록합니다.")
    @PostMapping
    public ApiResponse<Long> createSales(@Valid @RequestBody SalesCreateRequest request) {
        Long salesId = salesService.createSales(request);
        return ApiResponse.success("매출이 성공적으로 등록되었습니다.", salesId);
    }

    @Operation(summary = "주기별 매출 조회", description = "입력 주기와 기준일에 맞는 매출 데이터를 1건 조회합니다.")
    @GetMapping("/period")
    public ApiResponse<SalesPeriodResponse> getSalesByPeriod(
            @Parameter(description = "입력 주기 (DAILY, WEEKLY, MONTHLY, HOURLY)", example = "WEEKLY")
            @RequestParam CycleType cycleType,
            @Parameter(description = "조회 기준일 (YYYY-MM-DD)", example = "2026-05-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        return ApiResponse.success(salesService.getSalesByPeriod(cycleType, baseDate));
    }
}

