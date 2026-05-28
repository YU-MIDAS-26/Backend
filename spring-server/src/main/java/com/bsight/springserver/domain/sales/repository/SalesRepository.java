package com.bsight.springserver.domain.sales.repository;

import com.bsight.springserver.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SalesRepository extends JpaRepository<Sales, Long> {
    Optional<Sales> findBySaleDate(LocalDate saleDate);
}
