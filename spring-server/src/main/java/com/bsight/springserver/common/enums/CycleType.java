package com.bsight.springserver.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 매출 및 지출 입력 주기를 정의하는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum CycleType {
    MONTHLY("월간"),
    WEEKLY("주간"),
    DAILY("일간"),
    HOURLY("시간별");

    private final String description;
}
