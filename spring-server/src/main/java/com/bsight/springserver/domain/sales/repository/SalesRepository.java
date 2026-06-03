package com.bsight.springserver.domain.sales.repository;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.sales.entity.Sales;
import com.bsight.springserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, Long> {

    List<Sales> findByUserAndSaleDateAndCycleType(User user, LocalDate saleDate, CycleType cycleType);

    List<Sales> findByUserAndSaleDateBetweenAndCycleType(User user, LocalDate startDate, LocalDate endDate, CycleType cycleType);

    void deleteByUserAndSaleDateAndCycleType(User user, LocalDate saleDate, CycleType cycleType);
}
