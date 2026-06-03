package com.bsight.springserver.domain.cost.entity;

import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 임대료, 공과금 등 고정 지출 (사장님별 개별화)
 */
@Entity
@Getter
@Table(
        name = "fixed_costs",
        indexes = {
                @Index(name = "idx_fixed_cost_user_month", columnList = "user_id, targetYearMonth")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_fixed_cost_user_month",
                columnNames = {"user_id", "targetYearMonth"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedCost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String targetYearMonth; // 대상 년월 (YYYY-MM)

    @Column(nullable = false)
    private Long rent;

    @Column(nullable = false)
    private Long utilityCost;

    @Column(nullable = false)
    private Long totalCost;

    @Builder
    public FixedCost(User user, String targetYearMonth, Long rent, Long utilityCost) {
        this.user = user;
        this.targetYearMonth = targetYearMonth;
        this.rent = rent;
        this.utilityCost = utilityCost;
        this.totalCost = rent + utilityCost;
    }

    public void update(Long rent, Long utilityCost) {
        this.rent = rent;
        this.utilityCost = utilityCost;
        this.totalCost = rent + utilityCost;
    }
}
