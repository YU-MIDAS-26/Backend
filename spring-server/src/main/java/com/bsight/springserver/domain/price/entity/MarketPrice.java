package com.bsight.springserver.domain.price.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * KAMIS 일별 부류별 도·소매가격 정보 (응답 구조 그대로 매핑)
 */
@Getter
@Entity
@Table(
        name = "market_prices",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_market_price",
                columnNames = {"item_code", "kind_code", "rank_code", "product_cls_code", "collected_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── KAMIS 분류 정보 ────────────────────────────
    @Column(nullable = false, length = 100)
    private String itemName;        // 품목명 ("배추")

    @Column(length = 10)
    private String itemCode;        // 품목코드 ("211")

    @Column(length = 100)
    private String kindName;        // 품종명 ("봄(10kg(그물망 3포기))")

    @Column(length = 10)
    private String kindCode;        // 품종코드 ("01")

    @Column(name = "rank_name", length = 20)
    private String rank;            // 등급 ("상품"/"중품")

    @Column(length = 10)
    private String rankCode;        // 등급코드 ("04")

    @Column(length = 30)
    private String unit;            // 단위 ("10kg(그물망 3포기)")

    @Column(length = 10)
    private String categoryCode;    // 부류코드 (100~600)

    @Column(length = 10)
    private String productClsCode;  // 구분 (01:소매, 02:도매)

    // ── 수집 기준일 (요청한 p_regday) ──────────────
    @Column(nullable = false)
    private LocalDate collectedDate;

    // ── 시점별 가격 (KAMIS dpr1~dpr7) ──────────────
    @Column(precision = 15, scale = 2)
    private BigDecimal priceToday;    // dpr1: 당일

    @Column(precision = 15, scale = 2)
    private BigDecimal price1dAgo;    // dpr2: 1일전

    @Column(precision = 15, scale = 2)
    private BigDecimal price1wAgo;    // dpr3: 1주일전

    @Column(precision = 15, scale = 2)
    private BigDecimal price2wAgo;    // dpr4: 2주일전

    @Column(precision = 15, scale = 2)
    private BigDecimal price1mAgo;    // dpr5: 1개월전

    @Column(precision = 15, scale = 2)
    private BigDecimal price1yAgo;    // dpr6: 1년전

    @Column(precision = 15, scale = 2)
    private BigDecimal priceAvgYear;  // dpr7: 평년

    @Builder
    public MarketPrice(String itemName, String itemCode, String kindName, String kindCode,
                       String rank, String rankCode, String unit,
                       String categoryCode, String productClsCode, LocalDate collectedDate,
                       BigDecimal priceToday, BigDecimal price1dAgo, BigDecimal price1wAgo,
                       BigDecimal price2wAgo, BigDecimal price1mAgo, BigDecimal price1yAgo,
                       BigDecimal priceAvgYear) {
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.kindName = kindName;
        this.kindCode = kindCode;
        this.rank = rank;
        this.rankCode = rankCode;
        this.unit = unit;
        this.categoryCode = categoryCode;
        this.productClsCode = productClsCode;
        this.collectedDate = collectedDate;
        this.priceToday = priceToday;
        this.price1dAgo = price1dAgo;
        this.price1wAgo = price1wAgo;
        this.price2wAgo = price2wAgo;
        this.price1mAgo = price1mAgo;
        this.price1yAgo = price1yAgo;
        this.priceAvgYear = priceAvgYear;
    }
}
