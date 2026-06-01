package com.bsight.springserver.domain.payment.dto;

import com.bsight.springserver.domain.payment.entity.Channel;
import lombok.Builder;
import lombok.Getter;

/**
 * 채널별(매장/배달) 매출 집계
 */
@Getter
@Builder
public class ChannelBreakdownDto {
    private Channel channel;    // OFFLINE / DELIVERY
    private String label;       // "매장" / "배달" (한글 표시용)
    private Long amount;        // 해당 채널 매출 합계 (원)
    private Long count;         // 해당 채널 거래 건수
    private Double ratio;       // 전체 매출 대비 비율 (0.0 ~ 1.0)
}
