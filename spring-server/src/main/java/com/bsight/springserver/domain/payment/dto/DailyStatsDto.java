package com.bsight.springserver.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 일별 매출 집계 결과
 */
@Getter
@Builder
public class DailyStatsDto {
    private LocalDate date;     // 매출 발생일
    private Long amount;        // 일 매출 합계 (원)
    private Long count;         // 거래 건수
}
