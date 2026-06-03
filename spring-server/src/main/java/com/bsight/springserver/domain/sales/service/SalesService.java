package com.bsight.springserver.domain.sales.service;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.sales.dto.request.SalesCreateRequest;
import com.bsight.springserver.domain.sales.dto.response.SalesPeriodResponse;
import com.bsight.springserver.domain.sales.entity.Sales;
import com.bsight.springserver.domain.sales.entity.SalesHourly;
import com.bsight.springserver.domain.sales.repository.SalesRepository;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.bsight.springserver.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 매출 비즈니스 로직 (사장님별 개별화)
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SalesPeriodResponse getSalesPeriod(CycleType cycleType, LocalDate baseDate) {
        User user = getCurrentUser();
        LocalDate normalizedBaseDate = normalizeBaseDate(cycleType, baseDate);
        PeriodRange periodRange = resolvePeriodRange(cycleType, normalizedBaseDate);

        List<Sales> salesList = salesRepository.findByUserAndSaleDateAndCycleType(user, normalizedBaseDate, cycleType);
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

    @Transactional(readOnly = true)
    public Sales getLatestDailySales(LocalDate saleDate) {
        User user = getCurrentUser();
        return findLatestSales(user, saleDate, CycleType.DAILY);
    }

    @Transactional(readOnly = true)
    public List<Sales> getDailySalesBetween(LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        return salesRepository.findByUserAndSaleDateBetweenAndCycleType(user, startDate, endDate, CycleType.DAILY).stream()
                .collect(Collectors.groupingBy(Sales::getSaleDate))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .max(Comparator.comparing(Sales::getUpdatedAt))
                                .orElseThrow()
                ))
                .values()
                .stream()
                .toList();
    }

    public void deleteSalesPeriod(CycleType cycleType, LocalDate baseDate) {
        User user = getCurrentUser();
        LocalDate normalizedBaseDate = normalizeBaseDate(cycleType, baseDate);
        salesRepository.deleteByUserAndSaleDateAndCycleType(user, normalizedBaseDate, cycleType);
    }

    public Long saveDailySalesFromCsv(User user, LocalDate saleDate, Long totalAmount) {
        return saveOrUpdateSales(user, saleDate, CycleType.DAILY, totalAmount, List.of());
    }

    public Long createSales(SalesCreateRequest request) {
        User user = getCurrentUser();
        LocalDate normalizedBaseDate = normalizeBaseDate(request.getCycleType(), request.getSaleDate());
        List<SalesHourly> hourlySales = buildHourlySales(request);
        return saveOrUpdateSales(user, normalizedBaseDate, request.getCycleType(), request.getTotalAmount(), hourlySales);
    }

    private Long saveOrUpdateSales(User user, LocalDate saleDate, CycleType cycleType, Long totalAmount, List<SalesHourly> hourlySales) {
        List<Sales> existingSales = salesRepository.findByUserAndSaleDateAndCycleType(user, saleDate, cycleType);

        if (!existingSales.isEmpty()) {
            Sales latestSales = existingSales.stream()
                    .max(Comparator.comparing(Sales::getUpdatedAt))
                    .orElseThrow();
            latestSales.update(totalAmount);
            latestSales.replaceHourlySales(hourlySales);

            List<Sales> duplicates = existingSales.stream()
                    .filter(sales -> !sales.getId().equals(latestSales.getId()))
                    .toList();
            salesRepository.deleteAll(duplicates);
            return latestSales.getId();
        }

        Sales sales = Sales.builder()
                .user(user)
                .saleDate(saleDate)
                .cycleType(cycleType)
                .totalAmount(totalAmount)
                .build();
        hourlySales.forEach(sales::addHourlySale);

        return salesRepository.save(sales).getId();
    }

    private List<SalesHourly> buildHourlySales(SalesCreateRequest request) {
        List<SalesHourly> hourlySales = new ArrayList<>();
        if (request.getCycleType() == CycleType.HOURLY && request.getHourlySales() != null) {
            for (SalesCreateRequest.HourlySalesRequest hourlyDto : request.getHourlySales()) {
                hourlySales.add(SalesHourly.builder()
                        .saleHour(hourlyDto.getSaleHour())
                        .amount(hourlyDto.getAmount())
                        .build());
            }
        }
        return hourlySales;
    }

    private Sales findLatestSales(User user, LocalDate saleDate, CycleType cycleType) {
        return salesRepository.findByUserAndSaleDateAndCycleType(user, saleDate, cycleType).stream()
                .max(Comparator.comparing(Sales::getUpdatedAt))
                .orElse(null);
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

    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return userDetails.getUserId();
    }
}
