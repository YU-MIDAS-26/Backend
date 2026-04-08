package com.example.yumidasbackend.global.external.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverShoppingResponse {

    private int total;
    private int start;
    private int display;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String title;       // 상품명 (HTML 태그 포함)
        private String link;        // 상품 링크
        private String image;       // 이미지 URL
        private int lprice;         // 최저가
        private int hprice;         // 최고가
        private String mallName;    // 쇼핑몰 이름
        private String productId;
        private String category1;
        private String category2;
        private String category3;

        // HTML 태그 제거한 상품명
        public String getCleanTitle() {
            return title != null ? title.replaceAll("<[^>]+>", "") : "";
        }
    }
}
