package com.bsight.springserver.domain.cost.entity;

import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월 단위 고정비(월세, 공과금)를 저장하는 엔티티
 */
@Entity
@Getter
@Table(name = "fixed_costs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedCost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String targetYearMonth;

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
