package com.bsight.springserver.domain.cost.entity;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 재료비, 급여 등 변동 지출을 관리하는 엔티티
 */
@Entity
@Getter
@Table(name = "variable_costs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VariableCost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDate costDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleType cycleType;

    @Column(nullable = false)
    private Long ingredientCost; // 재료비

    @Column(nullable = false)
    private Long salaryCost; // 직원 급여

    @Column(nullable = false)
    private Long totalCost; // 변동비 합계

    @Builder
    public VariableCost(User user, LocalDate costDate, CycleType cycleType, Long ingredientCost, Long salaryCost) {
        this.user = user;
        this.costDate = costDate;
        this.cycleType = cycleType;
        this.ingredientCost = ingredientCost;
        this.salaryCost = salaryCost;
        this.totalCost = ingredientCost + salaryCost; // 서버 측 자동 계산
    }

    public void update(Long ingredientCost, Long salaryCost) {
        this.ingredientCost = ingredientCost;
        this.salaryCost = salaryCost;
        this.totalCost = ingredientCost + salaryCost;
    }

    public void update(LocalDate costDate, CycleType cycleType, Long ingredientCost, Long salaryCost) {
        this.costDate = costDate;
        this.cycleType = cycleType;
        this.ingredientCost = ingredientCost;
        this.salaryCost = salaryCost;
        this.totalCost = ingredientCost + salaryCost;
    }
}
