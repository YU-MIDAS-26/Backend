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
 * 재료비, 급여 등 변동 지출 (사장님별 개별화)
 */
@Entity
@Getter
@Table(
        name = "variable_costs",
        indexes = {
                @Index(name = "idx_variable_cost_user_date", columnList = "user_id, costDate")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VariableCost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate costDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleType cycleType;

    @Column(nullable = false)
    private Long ingredientCost;

    @Column(nullable = false)
    private Long salaryCost;

    @Column(nullable = false)
    private Long totalCost;

    @Builder
    public VariableCost(User user, LocalDate costDate, CycleType cycleType, Long ingredientCost, Long salaryCost) {
        this.user = user;
        this.costDate = costDate;
        this.cycleType = cycleType;
        this.ingredientCost = ingredientCost;
        this.salaryCost = salaryCost;
        this.totalCost = ingredientCost + salaryCost;
    }

    public void update(Long ingredientCost, Long salaryCost) {
        this.ingredientCost = ingredientCost;
        this.salaryCost = salaryCost;
        this.totalCost = ingredientCost + salaryCost;
    }
}
