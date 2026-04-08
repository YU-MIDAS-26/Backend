package com.example.yumidasbackend.domain.price.entity;

import com.example.yumidasbackend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "market_prices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(precision = 15, scale = 2)
    private BigDecimal avgPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxPrice;

    @Column(length = 30)
    private String unit;

    @Column(length = 100)
    private String marketName;

    @Column(nullable = false)
    private LocalDate collectedDate;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(length = 10)
    private String categoryCode;

    @Builder
    public MarketPrice(String itemName,
                       BigDecimal avgPrice,
                       BigDecimal minPrice,
                       BigDecimal maxPrice,
                       String unit,
                       String marketName,
                       LocalDate collectedDate,
                       String source,
                       String categoryCode) {
        this.itemName = itemName;
        this.avgPrice = avgPrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.unit = unit;
        this.marketName = marketName;
        this.collectedDate = collectedDate;
        this.source = source;
        this.categoryCode = categoryCode;
    }
}
