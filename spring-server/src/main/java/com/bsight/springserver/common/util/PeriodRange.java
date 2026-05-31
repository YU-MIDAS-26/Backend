package com.bsight.springserver.common.util;

import com.bsight.springserver.common.enums.CycleType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

/**
 * 주기(CycleType)와 기준일(baseDate)로 조회 구간을 계산하는 값 객체.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PeriodRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    /**
     * 기준일과 주기에 맞는 시작일/종료일을 계산합니다.
     */
    public static PeriodRange from(CycleType cycleType, LocalDate baseDate) {
        return switch (cycleType) {
            case DAILY, HOURLY -> new PeriodRange(baseDate, baseDate);
            case WEEKLY -> {
                LocalDate start = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new PeriodRange(start, start.plusDays(6));
            }
            case MONTHLY -> {
                YearMonth yearMonth = YearMonth.from(baseDate);
                yield new PeriodRange(yearMonth.atDay(1), yearMonth.atEndOfMonth());
            }
        };
    }

    /**
     * 저장 기준일을 주기에 맞춰 정규화합니다.
     * - DAILY/HOURLY: 입력일 그대로 사용
     * - WEEKLY/MONTHLY: 기간 시작일로 정규화
     */
    public LocalDate normalizedDateForSave(CycleType cycleType, LocalDate baseDate) {
        return switch (cycleType) {
            case DAILY, HOURLY -> baseDate;
            case WEEKLY, MONTHLY -> startDate;
        };
    }
}

