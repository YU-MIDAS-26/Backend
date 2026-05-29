package com.bsight.springserver.domain.sales.service;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.sales.dto.request.SalesCreateRequest;
import com.bsight.springserver.domain.sales.entity.Sales;
import com.bsight.springserver.domain.sales.entity.SalesHourly;
import com.bsight.springserver.domain.sales.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매출 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;

    /**
     * 매출 데이터를 저장합니다.
     * 주기가 HOURLY인 경우 하위 시간대별 매출도 함께 저장합니다.
     */
    public Long createSales(SalesCreateRequest request) {
        Sales sales = Sales.builder()
                .saleDate(request.getSaleDate())
                .cycleType(request.getCycleType())
                .totalAmount(request.getTotalAmount())
                .build();

        if (request.getCycleType() == CycleType.HOURLY && request.getHourlySales() != null) {
            for (SalesCreateRequest.HourlySalesRequest hourlyDto : request.getHourlySales()) {
                SalesHourly hourlyEntity = SalesHourly.builder()
                        .saleHour(hourlyDto.getSaleHour())
                        .amount(hourlyDto.getAmount())
                        .build();
                sales.addHourlySale(hourlyEntity);
            }
        }

        return salesRepository.save(sales).getId();
    }
}
