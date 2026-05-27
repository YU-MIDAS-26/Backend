package com.bsight.springserver.domain.cost.entity;

import com.bsight.springserver.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 임대료, 공과금 등 고정 지출을 관리하는 엔티티
 */
@Entity
@Getter
@Table(name = "fixed_costs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedCost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String targetYearMonth; // 대상 년월 (YYYY-MM)

    @Column(nullable = false)
    private Long rent; // 임대료

    @Column(nullable = false)
    private Long utilityCost; // 공과금

    @Column(nullable = false)
    private Long totalCost; // 고정비 합계

    @Builder
    public FixedCost(String targetYearMonth, Long rent, Long utilityCost) {
        this.targetYearMonth = targetYearMonth;
        this.rent = rent;
        this.utilityCost = utilityCost;
        this.totalCost = rent + utilityCost; // 서버 측 자동 계산
    }

    public void update(Long rent, Long utilityCost) {
        this.rent = rent;
        this.utilityCost = utilityCost;
        this.totalCost = rent + utilityCost;
    }
}
