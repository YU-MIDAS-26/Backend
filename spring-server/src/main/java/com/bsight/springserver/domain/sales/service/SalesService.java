package com.bsight.springserver.domain.sales.service;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.sales.dto.request.SalesCreateRequest;
import com.bsight.springserver.domain.sales.dto.response.SalesPeriodResponse;
import com.bsight.springserver.domain.sales.entity.Sales;
import com.bsight.springserver.domain.sales.entity.SalesHourly;
import com.bsight.springserver.domain.sales.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

/**
 * 매출 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;

    @Transactional(readOnly = true)
    public SalesPeriodResponse getSalesPeriod(CycleType cycleType, LocalDate baseDate) {
        LocalDate normalizedBaseDate = normalizeBaseDate(cycleType, baseDate);
        PeriodRange periodRange = resolvePeriodRange(cycleType, normalizedBaseDate);

        List<Sales> salesList = salesRepository.findBySaleDateAndCycleType(normalizedBaseDate, cycleType);
        Sales latestSales = salesList.stream()
                .max(Comparator.comparing(Sales::getUpdatedAt))
                .orElse(null);

        return SalesPeriodResponse.builder()
                .cycleType(cycleType)
                .baseDate(normalizedBaseDate)
                .periodStartDate(periodRange.startDate())
                .periodEndDate(periodRange.endDate())
                .totalAmount(latestSales != null ? latestSales.getTotalAmount() : 0L)
                .hourlySales(toHourlyResponses(latestSales, cycleType))
                .build();
    }

    public void deleteSalesPeriod(CycleType cycleType, LocalDate baseDate) {
        LocalDate normalizedBaseDate = normalizeBaseDate(cycleType, baseDate);
        salesRepository.deleteBySaleDateAndCycleType(normalizedBaseDate, cycleType);
    }

    /**
     * 매출 데이터를 저장합니다.
     * 주기가 HOURLY인 경우 하위 시간대별 매출도 함께 저장합니다.
     */
    public Long createSales(SalesCreateRequest request) {
        // 1. 기본 매출 엔티티 생성
        Sales sales = Sales.builder()
                .saleDate(request.getSaleDate())
                .cycleType(request.getCycleType())
                .totalAmount(request.getTotalAmount())
                .build();

        // 2. 주기가 HOURLY이고 상세 데이터가 있는 경우 연관관계 매핑
        if (request.getCycleType() == CycleType.HOURLY && request.getHourlySales() != null) {
            for (SalesCreateRequest.HourlySalesRequest hourlyDto : request.getHourlySales()) {
                SalesHourly hourlyEntity = SalesHourly.builder()
                        .saleHour(hourlyDto.getSaleHour())
                        .amount(hourlyDto.getAmount())
                        .build();
                sales.addHourlySale(hourlyEntity);
            }
        }

        // 3. 저장 (Cascade 설정으로 인해 SalesHourly도 자동 저장됨)
        return salesRepository.save(sales).getId();
    }

    private List<SalesPeriodResponse.HourlySalesResponse> toHourlyResponses(Sales sales, CycleType cycleType) {
        if (sales == null || cycleType != CycleType.HOURLY) {
            return List.of();
        }

        return sales.getHourlySales().stream()
                .sorted(Comparator.comparing(SalesHourly::getSaleHour))
                .map(hourly -> SalesPeriodResponse.HourlySalesResponse.builder()
                        .hour(hourly.getSaleHour())
                        .amount(hourly.getAmount())
                        .build())
                .toList();
    }

    private LocalDate normalizeBaseDate(CycleType cycleType, LocalDate baseDate) {
        if (cycleType == CycleType.WEEKLY) {
            return baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        if (cycleType == CycleType.MONTHLY) {
            return baseDate.withDayOfMonth(1);
        }
        return baseDate;
    }

    private PeriodRange resolvePeriodRange(CycleType cycleType, LocalDate baseDate) {
        return switch (cycleType) {
            case WEEKLY -> new PeriodRange(baseDate, baseDate.plusDays(6));
            case MONTHLY -> new PeriodRange(baseDate.withDayOfMonth(1), baseDate.withDayOfMonth(baseDate.lengthOfMonth()));
            case DAILY, HOURLY -> new PeriodRange(baseDate, baseDate);
        };
    }

    private record PeriodRange(LocalDate startDate, LocalDate endDate) {
    }
}
