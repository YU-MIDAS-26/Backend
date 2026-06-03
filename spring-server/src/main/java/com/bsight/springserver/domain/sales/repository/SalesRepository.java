package com.bsight.springserver.domain.sales.repository;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesRepository extends JpaRepository<Sales, Long> {
    Optional<Sales> findBySaleDate(LocalDate saleDate);

    List<Sales> findBySaleDateAndCycleType(LocalDate saleDate, CycleType cycleType);

    void deleteBySaleDateAndCycleType(LocalDate saleDate, CycleType cycleType);
}
