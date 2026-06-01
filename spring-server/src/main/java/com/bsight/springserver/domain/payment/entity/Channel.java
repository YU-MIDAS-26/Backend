package com.bsight.springserver.domain.payment.entity;

/**
 * 결제 채널 - 매장(OFFLINE) 또는 배달(DELIVERY)
 * CSV에서 다양한 채널명을 두 가지로 통합
 */
public enum Channel {
    OFFLINE,
    DELIVERY;

    /**
     * CSV의 주문채널 문자열을 Channel enum으로 변환
     * 매핑되지 않는 값은 null 반환 (호출부에서 skip 처리)
     */
    public static Channel from(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return switch (raw.trim()) {
            case "포스", "키오스크", "테이블오더", "매장" -> OFFLINE;
            case "배달", "배달앱", "배민", "쿠팡이츠", "요기요" -> DELIVERY;
            default -> null;
        };
    }
}
