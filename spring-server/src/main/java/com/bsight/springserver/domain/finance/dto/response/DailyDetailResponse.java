package com.bsight.springserver.domain.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 특정 날짜 클릭 시 보여줄 상세 데이터 응답 DTO
 */
@Getter
@Builder
@Schema(description = "특정 날짜 상세 데이터")
public class DailyDetailResponse {

    @Schema(description = "총 매출", example = "500000")
    private Long totalSales;

    @Schema(description = "총 지출", example = "350000")
    private Long totalExpense;

    @Schema(description = "변동비 합계", example = "200000")
    private Long variableCost;

    @Schema(description = "고정비 합계 (안분 계산값)", example = "150000")
    private Long fixedCost;

    @Schema(description = "순이익", example = "150000")
    private Long netProfit;

    @Schema(description = "시간대별 매출 목록 (입력 주기가 HOURLY일 때만 포함)")
    private List<HourlySalesDetail> hourlySales;

    @Getter
    @Builder
    @Schema(description = "시간대별 매출 상세")
    public static class HourlySalesDetail {
        @Schema(description = "시간대", example = "11:00")
        private String hour;
        @Schema(description = "매출액", example = "50000")
        private Long amount;
    }
}
