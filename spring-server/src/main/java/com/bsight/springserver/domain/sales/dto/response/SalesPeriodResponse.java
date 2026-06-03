package com.bsight.springserver.domain.sales.dto.response;

import com.bsight.springserver.common.enums.CycleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "기간별 매출 조회 응답 데이터")
public class SalesPeriodResponse {

    @Schema(description = "입력 주기", example = "DAILY")
    private CycleType cycleType;

    @Schema(description = "조회 기준일", example = "2026-06-02")
    private LocalDate baseDate;

    @Schema(description = "적용 시작일", example = "2026-06-02")
    private LocalDate periodStartDate;

    @Schema(description = "적용 종료일", example = "2026-06-02")
    private LocalDate periodEndDate;

    @Schema(description = "총 매출 금액", example = "500000")
    private Long totalAmount;

    @Schema(description = "시간대별 매출 목록")
    private List<HourlySalesResponse> hourlySales;

    @Getter
    @Builder
    @Schema(description = "시간대별 매출 응답 데이터")
    public static class HourlySalesResponse {

        @Schema(description = "시간대", example = "11:00")
        private String hour;

        @Schema(description = "매출액", example = "50000")
        private Long amount;
    }
}
