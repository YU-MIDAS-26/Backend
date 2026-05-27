package com.bsight.springserver.domain.sales.repository;

import com.bsight.springserver.domain.sales.entity.SalesHourly;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesHourlyRepository extends JpaRepository<SalesHourly, Long> {
}
