package com.bsight.springserver.domain.cost.service;

import com.bsight.springserver.domain.cost.dto.request.FixedCostRequest;
import com.bsight.springserver.domain.cost.dto.request.VariableCostRequest;
import com.bsight.springserver.domain.cost.entity.FixedCost;
import com.bsight.springserver.domain.cost.entity.VariableCost;
import com.bsight.springserver.domain.cost.repository.FixedCostRepository;
import com.bsight.springserver.domain.cost.repository.VariableCostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 지출(고정비, 변동비) 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CostService {

    private final FixedCostRepository fixedCostRepository;
    private final VariableCostRepository variableCostRepository;

    /**
     * 고정비를 등록하거나, 이미 해당 월의 데이터가 있다면 수정합니다.
     */
    public Long saveOrUpdateFixedCost(FixedCostRequest request) {
        Optional<FixedCost> existingCost = fixedCostRepository.findByTargetYearMonth(request.getTargetYearMonth());

        if (existingCost.isPresent()) {
            FixedCost cost = existingCost.get();
            cost.update(request.getRent(), request.getUtilityCost());
            return cost.getId();
        } else {
            FixedCost newCost = FixedCost.builder()
                    .targetYearMonth(request.getTargetYearMonth())
                    .rent(request.getRent())
                    .utilityCost(request.getUtilityCost())
                    .build();
            return fixedCostRepository.save(newCost).getId();
        }
    }

    /**
     * 변동비를 등록합니다.
     */
    public Long createVariableCost(VariableCostRequest request) {
        VariableCost variableCost = VariableCost.builder()
                .costDate(request.getCostDate())
                .cycleType(request.getCycleType())
                .ingredientCost(request.getIngredientCost())
                .salaryCost(request.getSalaryCost())
                .build();

        return variableCostRepository.save(variableCost).getId();
    }
}
