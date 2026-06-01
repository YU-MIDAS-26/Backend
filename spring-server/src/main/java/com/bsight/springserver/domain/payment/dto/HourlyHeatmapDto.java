package com.bsight.springserver.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 요일 × 시간대 매출 집계 (히트맵)
 */
@Getter
@Builder
public class HourlyHeatmapDto {
    private int dayOfWeek;   // 1=월, 2=화, ..., 7=일 (한국 관습)
    private int hour;        // 0 ~ 23
    private Long amount;     // 해당 셀의 매출 합계
    private Long count;      // 해당 셀의 거래 건수
}
