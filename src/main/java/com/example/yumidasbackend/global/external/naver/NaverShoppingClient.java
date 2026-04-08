package com.example.yumidasbackend.global.external.naver;

import com.example.yumidasbackend.global.common.exception.CustomException;
import com.example.yumidasbackend.global.common.exception.ErrorCode;
import com.example.yumidasbackend.global.external.naver.dto.NaverShoppingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverShoppingClient {

    private final WebClient naverWebClient;

    /**
     * 네이버 쇼핑 API로 상품 검색 (최저가 기준 정렬)
     *
     * @param query   검색어 (예: "국내산 돼지고기 삼겹살 1kg")
     * @param display 결과 수 (최대 100)
     */
    public NaverShoppingResponse search(String query, int display) {
        log.info("네이버 쇼핑 검색 - 쿼리: {}, 수량: {}", query, display);
        try {
            return naverWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("query", query)
                            .queryParam("display", display)
                            .queryParam("sort", "asc")   // 가격 오름차순 (공식: asc / dsc)
                            .build())
                    .retrieve()
                    .bodyToMono(NaverShoppingResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("네이버 쇼핑 API 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.NAVER_API_ERROR);
        }
    }

    /**
     * 검색어에 해당하는 최저가 아이템 목록 반환
     */
    public List<NaverShoppingResponse.Item> searchLowestPrice(String ingredientName, int topN) {
        NaverShoppingResponse response = search(ingredientName + " 식자재", topN);
        if (response == null || response.getItems() == null) {
            return List.of();
        }
        return response.getItems().stream()
                .filter(item -> item.getLprice() > 0)
                .sorted((a, b) -> Integer.compare(a.getLprice(), b.getLprice()))
                .limit(topN)
                .toList();
    }
}
