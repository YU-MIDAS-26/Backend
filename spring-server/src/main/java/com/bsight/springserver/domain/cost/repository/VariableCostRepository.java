package com.bsight.springserver.domain.cost.repository;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.cost.entity.VariableCost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VariableCostRepository extends JpaRepository<VariableCost, Long> {
    List<VariableCost> findByCostDateBetween(LocalDate startDate, LocalDate endDate);
    List<VariableCost> findByCycleTypeAndCostDateBetween(CycleType cycleType, LocalDate startDate, LocalDate endDate);
}
