package com.bsight.springserver.domain.finance.controller;

import com.bsight.springserver.domain.finance.dto.response.AiInsightResponse;
import com.bsight.springserver.domain.finance.dto.response.CalendarDailyResponse;
import com.bsight.springserver.domain.finance.dto.response.DailyDetailResponse;
import com.bsight.springserver.domain.finance.service.FinanceService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Finance", description = "금융 통계 및 AI 인사이트 API")
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @Operation(summary = "월별 캘린더 데이터 조회", description = "선택한 연/월의 날짜별 매출, 지출, 순이익 목록을 반환합니다.")
    @GetMapping("/calendar")
    public ApiResponse<List<CalendarDailyResponse>> getCalendarData(
            @Parameter(description = "조회 연월 (YYYY-MM)", example = "2026-05")
            @RequestParam String yearMonth) {
        return ApiResponse.success(financeService.getCalendarData(yearMonth));
    }

    @Operation(summary = "특정 날짜 상세 정보 조회", description = "캘린더에서 특정 날짜를 클릭했을 때의 상세 종합 지표를 반환합니다.")
    @GetMapping("/daily")
    public ApiResponse<DailyDetailResponse> getDailyDetail(
            @Parameter(description = "조회 날짜 (YYYY-MM-DD)", example = "2026-05-21")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(financeService.getDailyDetail(date));
    }

    @Operation(summary = "AI 경영 인사이트 조회", description = "당월 데이터를 기반으로 AI가 분석한 요약 및 추천 정보를 반환합니다.")
    @GetMapping("/ai-insight")
    public ApiResponse<AiInsightResponse> getAiInsight(
            @Parameter(description = "조회 연월 (YYYY-MM)", example = "2026-05")
            @RequestParam String yearMonth) {
        return ApiResponse.success(financeService.getAiInsight(yearMonth));
    }
}
