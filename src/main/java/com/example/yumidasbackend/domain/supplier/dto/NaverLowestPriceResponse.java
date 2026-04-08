package com.example.yumidasbackend.domain.supplier.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NaverLowestPriceResponse {
    private String ingredientName;
    private int topN;
    private List<NaverLowestPriceItemResponse> items;
}
