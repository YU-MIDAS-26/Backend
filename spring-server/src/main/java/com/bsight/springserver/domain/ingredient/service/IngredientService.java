package com.bsight.springserver.domain.ingredient.service;

import com.bsight.springserver.domain.ingredient.dto.CreateIngredientRequest;
import com.bsight.springserver.domain.ingredient.dto.IngredientResponse;
import com.bsight.springserver.domain.ingredient.dto.UpdateIngredientRequest;
import com.bsight.springserver.domain.ingredient.entity.Ingredient;
import com.bsight.springserver.domain.ingredient.repository.IngredientRepository;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.bsight.springserver.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;

    /**
     * 현재 로그인 사장님의 재료 등록
     */
    @Transactional
    public IngredientResponse create(CreateIngredientRequest request) {
        User user = getCurrentUser();
        String name = request.getName().trim();
        String unit = request.getUnit().trim();

        ingredientRepository.findByUserAndName(user, name)
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.INGREDIENT_ALREADY_EXISTS);
                });

        Ingredient ingredient = Ingredient.builder()
                .user(user)
                .name(name)
                .unit(unit)
                .build();

        return IngredientResponse.from(ingredientRepository.save(ingredient));
    }

    /**
     * 현재 로그인 사장님의 재료 목록 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public List<IngredientResponse> getAll() {
        User user = getCurrentUser();
        return ingredientRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(IngredientResponse::from)
                .toList();
    }

    /**
     * 현재 로그인 사장님의 재료 수정 (본인 재료만 수정 가능)
     * - 다른 재료와 이름 충돌 시 INGREDIENT_ALREADY_EXISTS 에러
     */
    @Transactional
    public IngredientResponse update(Long ingredientId, UpdateIngredientRequest request) {
        User user = getCurrentUser();
        Ingredient ingredient = ingredientRepository.findByIdAndUser(ingredientId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));

        String newName = request.getName().trim();
        String newUnit = request.getUnit().trim();

        if (!ingredient.getName().equals(newName)) {
            ingredientRepository.findByUserAndName(user, newName)
                    .ifPresent(existing -> {
                        throw new CustomException(ErrorCode.INGREDIENT_ALREADY_EXISTS);
                    });
        }

        ingredient.update(newName, newUnit);
        return IngredientResponse.from(ingredient);
    }

    /**
     * 현재 로그인 사장님의 재료 삭제 (본인 재료만 삭제 가능)
     */
    @Transactional
    public void delete(Long ingredientId) {
        User user = getCurrentUser();
        Ingredient ingredient = ingredientRepository.findByIdAndUser(ingredientId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.INGREDIENT_NOT_FOUND));
        ingredientRepository.delete(ingredient);
    }

    // ── 내부 헬퍼 ────────────────────────────────────

    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return userDetails.getUserId();
    }
}
