package com.bsight.springserver.domain.cost.dto.response;

import com.bsight.springserver.common.enums.CycleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 주기 기준 변동비 단건 조회 응답 DTO
 */
@Getter
@Builder
@Schema(description = "주기 기준 변동비 조회 응답 데이터")
public class VariableCostPeriodResponse {

    @Schema(description = "반영 주기", example = "MONTHLY")
    private CycleType cycleType;

    @Schema(description = "요청 기준일", example = "2026-05-01")
    private LocalDate baseDate;

    @Schema(description = "조회 구간 시작일", example = "2026-05-01")
    private LocalDate periodStartDate;

    @Schema(description = "조회 구간 종료일", example = "2026-05-31")
    private LocalDate periodEndDate;

    @Schema(description = "재료비", example = "200000")
    private Long ingredientCost;

    @Schema(description = "급여", example = "150000")
    private Long salaryCost;

    @Schema(description = "변동비 합계", example = "350000")
    private Long totalCost;
}

