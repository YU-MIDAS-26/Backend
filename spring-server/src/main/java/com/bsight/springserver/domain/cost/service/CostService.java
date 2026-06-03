package com.bsight.springserver.domain.cost.service;

import com.bsight.springserver.domain.cost.dto.request.FixedCostRequest;
import com.bsight.springserver.domain.cost.dto.request.VariableCostRequest;
import com.bsight.springserver.domain.cost.entity.FixedCost;
import com.bsight.springserver.domain.cost.entity.VariableCost;
import com.bsight.springserver.domain.cost.repository.FixedCostRepository;
import com.bsight.springserver.domain.cost.repository.VariableCostRepository;
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

import java.util.Optional;

/**
 * 지출 비즈니스 로직 (사장님별 개별화)
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CostService {

    private final FixedCostRepository fixedCostRepository;
    private final VariableCostRepository variableCostRepository;
    private final UserRepository userRepository;

    /**
     * 현재 로그인 사장님의 고정비 저장/수정 (해당 월 이미 있으면 update)
     */
    public Long saveOrUpdateFixedCost(FixedCostRequest request) {
        User user = getCurrentUser();
        Optional<FixedCost> existingCost = fixedCostRepository.findByUserAndTargetYearMonth(user, request.getTargetYearMonth());

        if (existingCost.isPresent()) {
            FixedCost cost = existingCost.get();
            cost.update(request.getRent(), request.getUtilityCost());
            return cost.getId();
        } else {
            FixedCost newCost = FixedCost.builder()
                    .user(user)
                    .targetYearMonth(request.getTargetYearMonth())
                    .rent(request.getRent())
                    .utilityCost(request.getUtilityCost())
                    .build();
            return fixedCostRepository.save(newCost).getId();
        }
    }

    /**
     * 현재 로그인 사장님의 변동비 등록
     */
    public Long createVariableCost(VariableCostRequest request) {
        User user = getCurrentUser();
        VariableCost variableCost = VariableCost.builder()
                .user(user)
                .costDate(request.getCostDate())
                .cycleType(request.getCycleType())
                .ingredientCost(request.getIngredientCost())
                .salaryCost(request.getSalaryCost())
                .build();

        return variableCostRepository.save(variableCost).getId();
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
