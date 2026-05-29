package com.bsight.springserver.domain.sales.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주기가 HOURLY일 때 시간대별 세부 매출을 저장하는 엔티티
 */
@Entity
@Getter
@Table(name = "sales_hourly")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesHourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_id", nullable = false)
    private Sales sales;

    @Column(nullable = false)
    private String saleHour; // 예: "11:00"

    @Column(nullable = false)
    private Long amount;

    @Builder
    public SalesHourly(String saleHour, Long amount) {
        this.saleHour = saleHour;
        this.amount = amount;
    }

    protected void setSales(Sales sales) {
        this.sales = sales;
    }
}
