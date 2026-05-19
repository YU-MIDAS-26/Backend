package com.bsight.springserver.domain.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverLowestPriceRequest {

    @NotBlank
    private String ingredientName;

    private Integer topN;
}
