package com.example.yumidasbackend.domain.ingredient.controller;

import com.example.yumidasbackend.domain.ingredient.dto.CreateIngredientRequest;
import com.example.yumidasbackend.domain.ingredient.dto.IngredientResponse;
import com.example.yumidasbackend.domain.ingredient.service.IngredientService;
import com.example.yumidasbackend.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    public ApiResponse<IngredientResponse> create(@Valid @RequestBody CreateIngredientRequest request) {
        return ApiResponse.success("재료가 등록되었습니다.", ingredientService.create(request));
    }

    @GetMapping
    public ApiResponse<List<IngredientResponse>> getAll() {
        return ApiResponse.success(ingredientService.getAll());
    }
}
