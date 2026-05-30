package com.bsight.springserver.domain.price.dto;

import com.bsight.springserver.domain.price.entity.MarketPrice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "KAMIS 시세 응답 데이터")
public class MarketPriceResponse {

    @Schema(description = "DB id", example = "1")
    private Long id;

    @Schema(description = "품목명", example = "배추")
    private String itemName;

    @Schema(description = "품목코드", example = "211")
    private String itemCode;

    @Schema(description = "품종명", example = "봄(10kg(그물망 3포기))")
    private String kindName;

    @Schema(description = "품종코드", example = "01")
    private String kindCode;

    @Schema(description = "등급", example = "상품")
    private String rank;

    @Schema(description = "등급코드", example = "04")
    private String rankCode;

    @Schema(description = "단위", example = "10kg(그물망 3포기)")
    private String unit;

    @Schema(description = "부류코드 (100~600)", example = "200")
    private String categoryCode;

    @Schema(description = "구분 (01:소매, 02:도매)", example = "02")
    private String productClsCode;

    @Schema(description = "수집 기준일", example = "2026-05-28")
    private LocalDate collectedDate;

    @Schema(description = "당일 가격", example = "8200")
    private BigDecimal priceToday;

    @Schema(description = "1일전 가격", example = "8400")
    private BigDecimal price1dAgo;

    @Schema(description = "1주일전 가격", example = "6000")
    private BigDecimal price1wAgo;

    @Schema(description = "2주일전 가격", example = "5000")
    private BigDecimal price2wAgo;

    @Schema(description = "1개월전 가격", example = "9199")
    private BigDecimal price1mAgo;

    @Schema(description = "1년전 가격", example = "6837")
    private BigDecimal price1yAgo;

    @Schema(description = "평년 가격", example = "8020")
    private BigDecimal priceAvgYear;

    public static MarketPriceResponse from(MarketPrice price) {
        return MarketPriceResponse.builder()
                .id(price.getId())
                .itemName(price.getItemName())
                .itemCode(price.getItemCode())
                .kindName(price.getKindName())
                .kindCode(price.getKindCode())
                .rank(price.getRank())
                .rankCode(price.getRankCode())
                .unit(price.getUnit())
                .categoryCode(price.getCategoryCode())
                .productClsCode(price.getProductClsCode())
                .collectedDate(price.getCollectedDate())
                .priceToday(price.getPriceToday())
                .price1dAgo(price.getPrice1dAgo())
                .price1wAgo(price.getPrice1wAgo())
                .price2wAgo(price.getPrice2wAgo())
                .price1mAgo(price.getPrice1mAgo())
                .price1yAgo(price.getPrice1yAgo())
                .priceAvgYear(price.getPriceAvgYear())
                .build();
    }
}
