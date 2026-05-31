package com.bsight.springserver.domain.cost.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 월 기준 고정비 조회 응답 DTO
 */
@Getter
@Builder
@Schema(description = "월 기준 고정비 조회 응답 데이터")
public class FixedCostResponse {

    @Schema(description = "대상 연월 (YYYY-MM)", example = "2026-05")
    private String targetYearMonth;

    @Schema(description = "월세", example = "1500000")
    private Long rent;

    @Schema(description = "공과금", example = "300000")
    private Long utilityCost;

    @Schema(description = "고정비 합계", example = "1800000")
    private Long totalCost;
}

