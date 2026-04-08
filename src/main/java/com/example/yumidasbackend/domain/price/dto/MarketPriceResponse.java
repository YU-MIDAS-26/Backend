package com.example.yumidasbackend.domain.price.dto;

import com.example.yumidasbackend.domain.price.entity.MarketPrice;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class MarketPriceResponse {
    private Long id;
    private String itemName;
    private BigDecimal avgPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String unit;
    private String marketName;
    private LocalDate collectedDate;
    private String source;
    private String categoryCode;

    public static MarketPriceResponse from(MarketPrice price) {
        return MarketPriceResponse.builder()
                .id(price.getId())
                .itemName(price.getItemName())
                .avgPrice(price.getAvgPrice())
                .minPrice(price.getMinPrice())
                .maxPrice(price.getMaxPrice())
                .unit(price.getUnit())
                .marketName(price.getMarketName())
                .collectedDate(price.getCollectedDate())
                .source(price.getSource())
                .categoryCode(price.getCategoryCode())
                .build();
    }
}
