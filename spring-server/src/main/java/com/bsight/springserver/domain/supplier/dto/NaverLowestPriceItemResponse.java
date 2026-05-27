package com.bsight.springserver.domain.supplier.dto;

import com.bsight.springserver.domain.external.naver.dto.NaverShoppingResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NaverLowestPriceItemResponse {
    private String title;
    private String link;
    private String mallName;
    private int lowestPrice;

    public static NaverLowestPriceItemResponse from(NaverShoppingResponse.Item item) {
        return NaverLowestPriceItemResponse.builder()
                .title(item.getCleanTitle())
                .link(item.getLink())
                .mallName(item.getMallName())
                .lowestPrice(item.getLprice())
                .build();
    }
}
