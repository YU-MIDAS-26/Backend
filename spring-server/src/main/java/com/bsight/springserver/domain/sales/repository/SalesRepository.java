package com.bsight.springserver.domain.sales.repository;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, Long> {
    List<Sales> findAllBySaleDate(LocalDate saleDate);
    List<Sales> findByCycleTypeAndSaleDateBetween(CycleType cycleType, LocalDate startDate, LocalDate endDate);
}
