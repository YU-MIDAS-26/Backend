package com.bsight.springserver.domain.sales.dto.request;

import com.bsight.springserver.common.enums.CycleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 매출 등록 요청을 위한 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "매출 등록 요청 데이터")
public class SalesCreateRequest {

    @NotNull(message = "날짜는 필수입니다.")
    @Schema(description = "매출 날짜", example = "2026-05-21")
    private LocalDate saleDate;

    @NotNull(message = "주기는 필수입니다.")
    @Schema(description = "입력 주기 (MONTHLY, WEEKLY, DAILY, HOURLY)", example = "HOURLY")
    private CycleType cycleType;

    @NotNull(message = "매출 금액은 필수입니다.")
    @Min(value = 0, message = "매출 금액은 0원 이상이어야 합니다.")
    @Schema(description = "총 매출 금액", example = "500000")
    private Long totalAmount;

    @Schema(description = "시간대별 매출 리스트 (주기가 HOURLY일 때만 사용)")
    private List<HourlySalesRequest> hourlySales;

    @Getter
    @NoArgsConstructor
    @Schema(description = "시간대별 매출 상세 데이터")
    public static class HourlySalesRequest {
        @Schema(description = "시간대", example = "11:00")
        private String saleHour;

        @Min(value = 0, message = "금액은 0원 이상이어야 합니다.")
        @Schema(description = "해당 시간대 매출 금액", example = "50000")
        private Long amount;
    }
}
