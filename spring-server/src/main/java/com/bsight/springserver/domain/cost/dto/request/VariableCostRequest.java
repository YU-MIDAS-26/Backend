package com.bsight.springserver.domain.cost.dto.request;

import com.bsight.springserver.common.enums.CycleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 변동비 등록 및 수정 요청을 위한 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "변동비 등록 요청 데이터")
public class VariableCostRequest {

    @NotNull(message = "비용 발생 날짜는 필수입니다.")
    @Schema(description = "비용 발생 날짜", example = "2026-05-21")
    private LocalDate costDate;

    @NotNull(message = "반영 주기는 필수입니다.")
    @Schema(description = "반영 주기 (MONTHLY, WEEKLY, DAILY)", example = "DAILY")
    private CycleType cycleType;

    @NotNull(message = "재료비는 필수입니다.")
    @Min(value = 0, message = "재료비는 0원 이상이어야 합니다.")
    @Schema(description = "재료비", example = "200000")
    private Long ingredientCost;

    @NotNull(message = "직원 급여는 필수입니다.")
    @Min(value = 0, message = "직원 급여는 0원 이상이어야 합니다.")
    @Schema(description = "직원 급여", example = "150000")
    private Long salaryCost;
}
