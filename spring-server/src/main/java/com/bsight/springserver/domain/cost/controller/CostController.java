package com.bsight.springserver.domain.cost.controller;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.cost.dto.request.FixedCostRequest;
import com.bsight.springserver.domain.cost.dto.request.VariableCostRequest;
import com.bsight.springserver.domain.cost.dto.response.FixedCostResponse;
import com.bsight.springserver.domain.cost.dto.response.VariableCostPeriodResponse;
import com.bsight.springserver.domain.cost.service.CostService;
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

@Tag(name = "Costs", description = "비용(고정비/변동비) 관리 API")
@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
public class CostController {

    private final CostService costService;

    @Operation(summary = "고정비 등록", description = "특정 연월의 고정비(월세, 공과금)를 등록합니다. 이미 데이터가 있으면 수정합니다.")
    @PostMapping("/fixed")
    public ApiResponse<Long> saveFixedCost(@Valid @RequestBody FixedCostRequest request) {
        Long costId = costService.saveOrUpdateFixedCost(request);
        return ApiResponse.success("고정비가 성공적으로 저장되었습니다.", costId);
    }

    @Operation(summary = "변동비 등록", description = "특정 주기/기준일의 변동비(재료비, 급여)를 등록합니다.")
    @PostMapping("/variable")
    public ApiResponse<Long> createVariableCost(@Valid @RequestBody VariableCostRequest request) {
        Long costId = costService.createVariableCost(request);
        return ApiResponse.success("변동비가 성공적으로 등록되었습니다.", costId);
    }

    @Operation(summary = "주기별 변동비 조회", description = "반영 주기와 기준일에 맞는 변동비 데이터를 1건 조회합니다.")
    @GetMapping("/variable/period")
    public ApiResponse<VariableCostPeriodResponse> getVariableCostByPeriod(
            @Parameter(description = "반영 주기 (DAILY, WEEKLY, MONTHLY)", example = "MONTHLY")
            @RequestParam CycleType cycleType,
            @Parameter(description = "조회 기준일 (YYYY-MM-DD)", example = "2026-05-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        return ApiResponse.success(costService.getVariableCostByPeriod(cycleType, baseDate));
    }

    @Operation(summary = "월별 고정비 조회", description = "대상 연월(YYYY-MM)의 고정비 데이터를 조회합니다.")
    @GetMapping("/fixed")
    public ApiResponse<FixedCostResponse> getFixedCost(
            @Parameter(description = "대상 연월 (YYYY-MM)", example = "2026-05")
            @RequestParam String yearMonth
    ) {
        return ApiResponse.success(costService.getFixedCost(yearMonth));
    }
}

