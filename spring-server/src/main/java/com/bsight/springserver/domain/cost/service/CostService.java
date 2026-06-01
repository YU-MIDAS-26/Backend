package com.bsight.springserver.domain.cost.service;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.common.util.PeriodRange;
import com.bsight.springserver.domain.cost.dto.request.FixedCostRequest;
import com.bsight.springserver.domain.cost.dto.request.VariableCostRequest;
import com.bsight.springserver.domain.cost.dto.response.FixedCostResponse;
import com.bsight.springserver.domain.cost.dto.response.VariableCostPeriodResponse;
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

import java.time.LocalDate;
import java.util.Comparator;

/**
 * 비용(고정비/변동비) 저장/조회 비즈니스 로직을 처리하는 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CostService {

    private final FixedCostRepository fixedCostRepository;
    private final VariableCostRepository variableCostRepository;
    private final UserRepository userRepository;

    /**
     * 현재 로그인 사용자의 고정비를 저장하거나, 이미 해당 월 데이터가 있으면 수정합니다.
     */
    public Long saveOrUpdateFixedCost(FixedCostRequest request) {
        User user = getCurrentUser();

        return fixedCostRepository.findByUserAndTargetYearMonth(user, request.getTargetYearMonth())
                .map(cost -> {
                    cost.update(request.getRent(), request.getUtilityCost());
                    return cost.getId();
                })
                .orElseGet(() -> {
                    FixedCost newCost = FixedCost.builder()
                            .user(user)
                            .targetYearMonth(request.getTargetYearMonth())
                            .rent(request.getRent())
                            .utilityCost(request.getUtilityCost())
                            .build();
                    return fixedCostRepository.save(newCost).getId();
                });
    }

    /**
     * 현재 로그인 사용자의 변동비를 주기/기준일에 맞춰 저장합니다.
     * - 동일 주기/동일 기간 데이터가 있으면 update
     * - 없으면 insert
     */
    public Long createVariableCost(VariableCostRequest request) {
        User user = getCurrentUser();
        CycleType cycleType = request.getCycleType();
        PeriodRange periodRange = PeriodRange.from(cycleType, request.getCostDate());
        LocalDate normalizedCostDate = periodRange.normalizedDateForSave(cycleType, request.getCostDate());

        VariableCost variableCost = variableCostRepository.findByUserAndCycleTypeAndCostDateBetween(
                        user,
                        cycleType,
                        periodRange.getStartDate(),
                        periodRange.getEndDate())
                .stream()
                .max(latestUpdatedVariableCostComparator())
                .orElseGet(() -> VariableCost.builder()
                        .user(user)
                        .costDate(normalizedCostDate)
                        .cycleType(cycleType)
                        .ingredientCost(0L)
                        .salaryCost(0L)
                        .build());

        variableCost.update(
                normalizedCostDate,
                cycleType,
                request.getIngredientCost(),
                request.getSalaryCost()
        );

        return variableCostRepository.save(variableCost).getId();
    }

    /**
     * 주기/기준일에 해당하는 현재 로그인 사용자의 변동비 1건을 조회합니다.
     * - 동일 기간에 여러 건이 있으면 최근 수정 데이터를 반환
     * - 데이터가 없으면 0원 응답 반환
     */
    @Transactional(readOnly = true)
    public VariableCostPeriodResponse getVariableCostByPeriod(CycleType cycleType, LocalDate baseDate) {
        User user = getCurrentUser();
        PeriodRange periodRange = PeriodRange.from(cycleType, baseDate);

        return variableCostRepository.findByUserAndCycleTypeAndCostDateBetween(
                        user,
                        cycleType,
                        periodRange.getStartDate(),
                        periodRange.getEndDate())
                .stream()
                .max(latestUpdatedVariableCostComparator())
                .map(variableCost -> VariableCostPeriodResponse.builder()
                        .cycleType(cycleType)
                        .baseDate(baseDate)
                        .periodStartDate(periodRange.getStartDate())
                        .periodEndDate(periodRange.getEndDate())
                        .ingredientCost(variableCost.getIngredientCost())
                        .salaryCost(variableCost.getSalaryCost())
                        .totalCost(variableCost.getTotalCost())
                        .build())
                .orElseGet(() -> VariableCostPeriodResponse.builder()
                        .cycleType(cycleType)
                        .baseDate(baseDate)
                        .periodStartDate(periodRange.getStartDate())
                        .periodEndDate(periodRange.getEndDate())
                        .ingredientCost(0L)
                        .salaryCost(0L)
                        .totalCost(0L)
                        .build());
    }

    /**
     * 대상 연월의 현재 로그인 사용자 고정비를 조회합니다.
     * 데이터가 없으면 0원 응답을 반환합니다.
     */
    @Transactional(readOnly = true)
    public FixedCostResponse getFixedCost(String targetYearMonth) {
        User user = getCurrentUser();

        return fixedCostRepository.findByUserAndTargetYearMonth(user, targetYearMonth)
                .map(fixedCost -> FixedCostResponse.builder()
                        .targetYearMonth(targetYearMonth)
                        .rent(fixedCost.getRent())
                        .utilityCost(fixedCost.getUtilityCost())
                        .totalCost(fixedCost.getTotalCost())
                        .build())
                .orElseGet(() -> FixedCostResponse.builder()
                        .targetYearMonth(targetYearMonth)
                        .rent(0L)
                        .utilityCost(0L)
                        .totalCost(0L)
                        .build());
    }

    private Comparator<VariableCost> latestUpdatedVariableCostComparator() {
        return Comparator.comparing(VariableCost::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(VariableCost::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();

        return userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getUserId();
    }
}

