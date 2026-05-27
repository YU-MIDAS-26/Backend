package com.bsight.springserver.domain.cost.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고정비 등록 및 수정 요청을 위한 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "고정비 등록 요청 데이터")
public class FixedCostRequest {

    @NotBlank(message = "대상 년월은 필수입니다.")
    @Schema(description = "대상 년월", example = "2026-05")
    private String targetYearMonth;

    @NotNull(message = "임대료는 필수입니다.")
    @Min(value = 0, message = "임대료는 0원 이상이어야 합니다.")
    @Schema(description = "임대료", example = "1500000")
    private Long rent;

    @NotNull(message = "공과금은 필수입니다.")
    @Min(value = 0, message = "공과금은 0원 이상이어야 합니다.")
    @Schema(description = "공과금", example = "300000")
    private Long utilityCost;
}
