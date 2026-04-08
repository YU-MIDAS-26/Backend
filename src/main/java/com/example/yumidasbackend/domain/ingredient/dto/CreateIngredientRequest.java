package com.example.yumidasbackend.domain.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateIngredientRequest {

    @NotBlank(message = "재료명은 필수입니다.")
    private String name;

    @NotBlank(message = "단위는 필수입니다.")
    private String unit;
}
