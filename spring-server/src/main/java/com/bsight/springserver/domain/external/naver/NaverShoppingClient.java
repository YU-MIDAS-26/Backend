package com.bsight.springserver.domain.external.naver;

import com.bsight.springserver.domain.external.naver.dto.NaverShoppingResponse;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // ── 품질 필터 ─────────────────────────────────────────────

    /** 식자재로 인정할 카테고리 (네이버 쇼핑 category1 기준) */
    private static final Set<String> CATEGORY1_ALLOW = Set.of("식품");

    /**
     * 가공품/비식자재 키워드 — 제목에 포함되면 제외.
     * 예: "사과" 검색 시 "사과주스/사과즙/사과식초/사과잼/사과가루" 등 차단.
     */
    private static final List<String> TITLE_BLACKLIST = List.of(
            "주스", "음료", "즙", "농축액", "추출액", "추출물", "원액",
            "분말", "가루", "환", "캡슐", "정", "보충제",
            "잼", "시럽", "퓨레", "퓌레",
            "식초", "초절임", "절임", "장아찌",
            "차(", "허브차", "녹차", "홍차",
            "비누", "샴푸", "장난감", "케이스", "스티커",
            "쿠키", "과자", "초콜릿", "사탕", "젤리", "케이크", "빵",
            "샘플", "체험", "증정"
    );

    /** 제목에서 무게/용량 추출 — "10kg", "500g", "1.5L" 등 */
    private static final Pattern WEIGHT_PATTERN = Pattern.compile(
            "(\\d+(?:[\\.,]\\d+)?)\\s*(kg|g|l|ml)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 검색어에 해당하는 최저가 아이템 목록 반환.
     * - 후보 풀 100건으로 확장 (display=100)
     * - 카테고리 화이트리스트 (식품)
     * - 가공품/비식자재 키워드 블랙리스트
     * - 1kg 환산 단위가격 기준 오름차순 정렬
     * - 블랙리스트로 0건이 되면 카테고리만 유지하고 폴백 (예: "후추", "고춧가루")
     */
    public List<NaverShoppingResponse.Item> searchLowestPrice(String ingredientName, int topN) {
        NaverShoppingResponse response = search(ingredientName + " 식자재", 100);
        if (response == null || response.getItems() == null) {
            return List.of();
        }

        List<NaverShoppingResponse.Item> strict = response.getItems().stream()
                .filter(item -> item.getLprice() > 0)
                .filter(NaverShoppingClient::isFoodCategory)
                .filter(NaverShoppingClient::isNotProcessedProduct)
                .sorted(Comparator
                        .comparingDouble(NaverShoppingClient::unitPricePerKg)
                        .thenComparingInt(NaverShoppingResponse.Item::getLprice))
                .limit(topN)
                .toList();

        if (!strict.isEmpty()) {
            return strict;
        }

        // 폴백: 블랙리스트 결과가 0건이면 카테고리만 유지 (가공된 형태가 정상인 재료 대응)
        log.info("네이버 최저가 - 블랙리스트 적용 후 0건, 카테고리만 유지하고 폴백: {}", ingredientName);
        return response.getItems().stream()
                .filter(item -> item.getLprice() > 0)
                .filter(NaverShoppingClient::isFoodCategory)
                .sorted(Comparator
                        .comparingDouble(NaverShoppingClient::unitPricePerKg)
                        .thenComparingInt(NaverShoppingResponse.Item::getLprice))
                .limit(topN)
                .toList();
    }

    private static boolean isFoodCategory(NaverShoppingResponse.Item item) {
        String c1 = item.getCategory1();
        return c1 != null && CATEGORY1_ALLOW.contains(c1);
    }

    private static boolean isNotProcessedProduct(NaverShoppingResponse.Item item) {
        String title = item.getCleanTitle();
        if (title.isBlank()) return true;
        for (String bad : TITLE_BLACKLIST) {
            if (title.contains(bad)) return false;
        }
        return true;
    }

    /**
     * 제목에서 무게/용량 정보를 추출해 1kg(또는 1L) 환산 단가를 계산.
     * 추출 실패 시 lprice를 그대로 반환 (정렬 후순위로 밀려남).
     */
    private static double unitPricePerKg(NaverShoppingResponse.Item item) {
        String title = item.getCleanTitle();
        Matcher m = WEIGHT_PATTERN.matcher(title);
        double bestKg = -1;
        while (m.find()) {
            double value = Double.parseDouble(m.group(1).replace(",", "."));
            String unit = m.group(2).toLowerCase();
            double kg = switch (unit) {
                case "kg", "l" -> value;
                case "g", "ml" -> value / 1000.0;
                default -> 0;
            };
            if (kg > bestKg) bestKg = kg;
        }
        if (bestKg <= 0) {
            // 무게 정보 없으면 단가 산정 불가 → 큰 값으로 후순위
            return Double.MAX_VALUE / 2;
        }
        return item.getLprice() / bestKg;
    }
}
