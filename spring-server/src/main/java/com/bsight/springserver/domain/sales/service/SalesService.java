package com.bsight.springserver.domain.sales.service;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.common.util.PeriodRange;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * 매출 저장/조회 비즈니스 로직을 처리하는 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final UserRepository userRepository;

    /**
     * 주기와 기준일에 맞춰 현재 로그인 사용자의 매출 데이터를 저장합니다.
     * - 동일 주기/동일 기간 데이터가 있으면 update
     * - 없으면 insert
     */
    public Long createSales(SalesCreateRequest request) {
        User user = getCurrentUser();
        CycleType cycleType = request.getCycleType();
        PeriodRange periodRange = PeriodRange.from(cycleType, request.getSaleDate());
        LocalDate normalizedSaleDate = periodRange.normalizedDateForSave(cycleType, request.getSaleDate());

        Sales sales = salesRepository.findByUserAndCycleTypeAndSaleDateBetween(
                        user,
                        cycleType,
                        periodRange.getStartDate(),
                        periodRange.getEndDate())
                .stream()
                .max(latestUpdatedSalesComparator())
                .orElseGet(() -> Sales.builder()
                        .user(user)
                        .saleDate(normalizedSaleDate)
                        .cycleType(cycleType)
                        .totalAmount(0L)
                        .build());

        sales.update(normalizedSaleDate, cycleType, request.getTotalAmount());
        sales.clearHourlySales();

        if (cycleType == CycleType.HOURLY && request.getHourlySales() != null) {
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

    /**
     * 주기/기준일에 해당하는 현재 로그인 사용자의 매출 1건을 조회합니다.
     * - 동일 기간에 여러 건이 있으면 최근 수정 데이터를 반환
     * - 데이터가 없으면 0원 응답 반환
     */
    @Transactional(readOnly = true)
    public SalesPeriodResponse getSalesByPeriod(CycleType cycleType, LocalDate baseDate) {
        User user = getCurrentUser();
        PeriodRange periodRange = PeriodRange.from(cycleType, baseDate);

        return salesRepository.findByUserAndCycleTypeAndSaleDateBetween(
                        user,
                        cycleType,
                        periodRange.getStartDate(),
                        periodRange.getEndDate())
                .stream()
                .max(latestUpdatedSalesComparator())
                .map(sales -> mapToSalesPeriodResponse(sales, cycleType, baseDate, periodRange))
                .orElseGet(() -> emptySalesPeriodResponse(cycleType, baseDate, periodRange));
    }

    private SalesPeriodResponse mapToSalesPeriodResponse(
            Sales sales,
            CycleType cycleType,
            LocalDate baseDate,
            PeriodRange periodRange
    ) {
        List<SalesPeriodResponse.HourlySalesDetail> hourlyDetails = sales.getHourlySales().stream()
                .sorted(Comparator.comparing(SalesHourly::getSaleHour))
                .map(hourly -> SalesPeriodResponse.HourlySalesDetail.builder()
                        .hour(hourly.getSaleHour())
                        .amount(hourly.getAmount())
                        .build())
                .toList();

        return SalesPeriodResponse.builder()
                .cycleType(cycleType)
                .baseDate(baseDate)
                .periodStartDate(periodRange.getStartDate())
                .periodEndDate(periodRange.getEndDate())
                .totalAmount(sales.getTotalAmount())
                .hourlySales(hourlyDetails)
                .build();
    }

    private SalesPeriodResponse emptySalesPeriodResponse(
            CycleType cycleType,
            LocalDate baseDate,
            PeriodRange periodRange
    ) {
        return SalesPeriodResponse.builder()
                .cycleType(cycleType)
                .baseDate(baseDate)
                .periodStartDate(periodRange.getStartDate())
                .periodEndDate(periodRange.getEndDate())
                .totalAmount(0L)
                .hourlySales(List.of())
                .build();
    }

    private Comparator<Sales> latestUpdatedSalesComparator() {
        return Comparator.comparing(Sales::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Sales::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();

        return userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getUserId();
    }
}

