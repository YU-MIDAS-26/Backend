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
                            .queryParam("sort", "sim")   // 관련도 정렬 (asc는 10원짜리 스팸이 상위에 노출됨)
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
     * 1차 식자재로 인정할 category2 화이트리스트.
     * (네이버 쇼핑 카테고리 체계상 "음료"가 category1=식품 아래에 있어
     *  category1 단독 필터로는 사과주스/음료가 통과되는 문제 보정)
     */
    private static final Set<String> CATEGORY2_ALLOW = Set.of(
            "농산물", "축산물", "수산물", "쌀/잡곡",
            "조미료", "식용유", "장류", "젓갈/액젓",
            "건어물", "수산가공품",
            "냉장식품", "냉동식품"
    );

    /** category2 블랙리스트 (확실히 식자재 아닌 카테고리) */
    private static final Set<String> CATEGORY2_BLACK = Set.of(
            "음료", "주류", "차/커피", "다이어트식품",
            "건강식품", "베이비/유아식품",
            "과자/베이커리", "초콜릿/캔디",
            "통조림/햄/소시지", "면류"  // 가공품 - 필요시 화이트로 옮길 수 있음
    );

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
    /** 비정상 스팸 가격 컷오프 (10원짜리 광고/미끼 차단) */
    private static final int MIN_VALID_PRICE = 1000;

    public List<NaverShoppingResponse.Item> searchLowestPrice(String ingredientName, int topN) {
        // "식자재" 부가 키워드 제거 — 음료 회사들이 "식자재"로 마케팅된 가공품을 끌어옴
        NaverShoppingResponse response = search(ingredientName, 100);
        if (response == null || response.getItems() == null) {
            return List.of();
        }

        List<NaverShoppingResponse.Item> strict = response.getItems().stream()
                .filter(item -> item.getLprice() >= MIN_VALID_PRICE)
                .filter(NaverShoppingClient::isFoodCategory)
                .filter(NaverShoppingClient::isAllowedSubCategory)
                .filter(NaverShoppingClient::isNotProcessedProduct)
                .sorted(Comparator.comparingInt(NaverShoppingResponse.Item::getLprice))
                .limit(topN)
                .toList();

        if (!strict.isEmpty()) {
            return strict;
        }

        // 폴백 1: 블랙리스트만 풀고 category2 화이트리스트는 유지
        log.info("네이버 최저가 - 키워드 블랙리스트 적용 후 0건, 블랙리스트 풀고 폴백: {}", ingredientName);
        List<NaverShoppingResponse.Item> fallback1 = response.getItems().stream()
                .filter(item -> item.getLprice() >= MIN_VALID_PRICE)
                .filter(NaverShoppingClient::isFoodCategory)
                .filter(NaverShoppingClient::isAllowedSubCategory)
                .sorted(Comparator.comparingInt(NaverShoppingResponse.Item::getLprice))
                .limit(topN)
                .toList();
        if (!fallback1.isEmpty()) {
            return fallback1;
        }

        // 폴백 2: category2 화이트리스트도 풀고 category2 블랙만 유지 (음료/주류 등은 끝까지 차단)
        log.info("네이버 최저가 - category2 화이트 0건, 블랙만 적용하고 폴백: {}", ingredientName);
        return response.getItems().stream()
                .filter(item -> item.getLprice() >= MIN_VALID_PRICE)
                .filter(NaverShoppingClient::isFoodCategory)
                .filter(NaverShoppingClient::isNotBlockedSubCategory)
                .sorted(Comparator.comparingInt(NaverShoppingResponse.Item::getLprice))
                .limit(topN)
                .toList();
    }

    private static boolean isFoodCategory(NaverShoppingResponse.Item item) {
        String c1 = item.getCategory1();
        return c1 != null && CATEGORY1_ALLOW.contains(c1);
    }

    /** category2가 식자재 화이트리스트에 포함되면 true */
    private static boolean isAllowedSubCategory(NaverShoppingResponse.Item item) {
        String c2 = item.getCategory2();
        return c2 != null && CATEGORY2_ALLOW.contains(c2);
    }

    /** category2가 블랙리스트(음료/주류 등)이면 false */
    private static boolean isNotBlockedSubCategory(NaverShoppingResponse.Item item) {
        String c2 = item.getCategory2();
        return c2 == null || !CATEGORY2_BLACK.contains(c2);
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
