package com.bsight.springserver.domain.ingredient.controller;

import com.bsight.springserver.domain.ingredient.dto.CreateIngredientRequest;
import com.bsight.springserver.domain.ingredient.dto.IngredientResponse;
import com.bsight.springserver.domain.ingredient.service.IngredientService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Ingredient", description = "사장님 등록 재료 API (개인화)")
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @Operation(summary = "재료 등록", description = "현재 로그인 사장님의 재료를 등록합니다. 같은 사장님 내에서 재료명 중복 불가.")
    @PostMapping
    public ApiResponse<IngredientResponse> create(@Valid @RequestBody CreateIngredientRequest request) {
        return ApiResponse.success("재료가 등록되었습니다.", ingredientService.create(request));
    }

    @Operation(summary = "재료 목록 조회", description = "현재 로그인 사장님의 재료만 최신순으로 반환합니다.")
    @GetMapping
    public ApiResponse<List<IngredientResponse>> getAll() {
        return ApiResponse.success(ingredientService.getAll());
    }

    @Operation(summary = "재료 삭제", description = "본인이 등록한 재료만 삭제 가능합니다.")
    @DeleteMapping("/{ingredientId}")
    public ApiResponse<Void> delete(@PathVariable Long ingredientId) {
        ingredientService.delete(ingredientId);
        return ApiResponse.success("재료가 삭제되었습니다.", null);
    }
}
