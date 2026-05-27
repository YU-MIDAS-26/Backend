package com.bsight.springserver.domain.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 캘린더의 각 날짜별 요약 지표를 담는 응답 DTO
 */
@Getter
@Builder
@Schema(description = "캘린더 날짜별 요약 데이터")
public class CalendarDailyResponse {

    @Schema(description = "날짜", example = "2026-05-21")
    private LocalDate date;

    @Schema(description = "해당 날짜 총 매출", example = "500000")
    private Long dailySales;

    @Schema(description = "해당 날짜 총 지출 (변동비 + 안분된 고정비)", example = "350000")
    private Long dailyExpense;

    @Schema(description = "해당 날짜 순이익", example = "150000")
    private Long dailyProfit;
}
