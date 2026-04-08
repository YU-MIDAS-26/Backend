package com.example.yumidasbackend.domain.ingredient.service;

import com.example.yumidasbackend.domain.ingredient.dto.CreateIngredientRequest;
import com.example.yumidasbackend.domain.ingredient.dto.IngredientResponse;
import com.example.yumidasbackend.domain.ingredient.entity.Ingredient;
import com.example.yumidasbackend.domain.ingredient.repository.IngredientRepository;
import com.example.yumidasbackend.global.common.exception.CustomException;
import com.example.yumidasbackend.global.common.exception.ErrorCode;
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
