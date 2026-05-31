package com.bsight.springserver.domain.sales.dto.response;

import com.bsight.springserver.common.enums.CycleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 주기 기준 매출 단건 조회 응답 DTO
 */
@Getter
@Builder
@Schema(description = "주기 기준 매출 조회 응답 데이터")
public class SalesPeriodResponse {

    @Schema(description = "입력 주기", example = "WEEKLY")
    private CycleType cycleType;

    @Schema(description = "요청 기준일", example = "2026-05-01")
    private LocalDate baseDate;

    @Schema(description = "조회 구간 시작일", example = "2026-04-27")
    private LocalDate periodStartDate;

    @Schema(description = "조회 구간 종료일", example = "2026-05-03")
    private LocalDate periodEndDate;

    @Schema(description = "총 매출 금액", example = "500000")
    private Long totalAmount;

    @Schema(description = "시간대별 매출 목록 (주기가 HOURLY일 때만 포함)")
    private List<HourlySalesDetail> hourlySales;

    @Getter
    @Builder
    @Schema(description = "시간대별 매출 상세")
    public static class HourlySalesDetail {
        @Schema(description = "시간대", example = "11:00")
        private String hour;

        @Schema(description = "해당 시간대 매출 금액", example = "50000")
        private Long amount;
    }
}

