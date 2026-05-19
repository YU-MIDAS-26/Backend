package com.bsight.springserver.domain.ingredient.service;

import com.bsight.springserver.domain.ingredient.dto.CreateIngredientRequest;
import com.bsight.springserver.domain.ingredient.dto.IngredientResponse;
import com.bsight.springserver.domain.ingredient.entity.Ingredient;
import com.bsight.springserver.domain.ingredient.repository.IngredientRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Transactional
    public IngredientResponse create(CreateIngredientRequest request) {
        ingredientRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.INGREDIENT_ALREADY_EXISTS);
                });

        Ingredient ingredient = Ingredient.builder()
                .name(request.getName().trim())
                .unit(request.getUnit().trim())
                .build();

        return IngredientResponse.from(ingredientRepository.save(ingredient));
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> getAll() {
        return ingredientRepository.findAll().stream()
                .map(IngredientResponse::from)
                .toList();
    }
}
