package com.example.yumidasbackend.domain.ingredient.dto;

import com.example.yumidasbackend.domain.ingredient.entity.Ingredient;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IngredientResponse {
    private Long id;
    private String name;
    private String unit;

    public static IngredientResponse from(Ingredient ingredient) {
        return IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .unit(ingredient.getUnit())
                .build();
    }
}
