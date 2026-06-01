package com.bsight.springserver.domain.sales.entity;

import com.bsight.springserver.common.enums.CycleType;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 일별/월별 매출 정보를 저장하는 엔티티
 */
@Entity
@Getter
@Table(name = "sales")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sales extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleType cycleType;

    @Column(nullable = false)
    private Long totalAmount;

    @OneToMany(mappedBy = "sales", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesHourly> hourlySales = new ArrayList<>();

    @Builder
    public Sales(User user, LocalDate saleDate, CycleType cycleType, Long totalAmount) {
        this.user = user;
        this.saleDate = saleDate;
        this.cycleType = cycleType;
        this.totalAmount = totalAmount;
    }

    public void addHourlySale(SalesHourly hourlySale) {
        this.hourlySales.add(hourlySale);
        hourlySale.setSales(this);
    }

    public void update(LocalDate saleDate, CycleType cycleType, Long totalAmount) {
        this.saleDate = saleDate;
        this.cycleType = cycleType;
        this.totalAmount = totalAmount;
    }

    public void clearHourlySales() {
        this.hourlySales.clear();
    }
}
