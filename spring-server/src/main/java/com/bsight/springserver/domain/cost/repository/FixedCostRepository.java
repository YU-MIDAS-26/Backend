package com.bsight.springserver.domain.cost.repository;

import com.bsight.springserver.domain.cost.entity.FixedCost;
import com.bsight.springserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FixedCostRepository extends JpaRepository<FixedCost, Long> {

    Optional<FixedCost> findByUserAndTargetYearMonth(User user, String targetYearMonth);
}

