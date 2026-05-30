package com.bsight.springserver.domain.price.service;

import com.bsight.springserver.domain.price.dto.MarketPriceResponse;
import com.bsight.springserver.domain.price.dto.PriceCollectResult;
import com.bsight.springserver.domain.price.entity.MarketPrice;
import com.bsight.springserver.domain.price.repository.MarketPriceRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketPriceService {

    private static final String[] CATEGORY_CODES = {"100", "200", "300", "400", "500", "600"};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WebClient.Builder webClientBuilder;
    private final MarketPriceRepository marketPriceRepository;

    @Value("${kamis.base-url:https://www.kamis.or.kr/service/price/xml.do}")
    private String baseUrl;

    @Value("${kamis.cert-key:}")
    private String certKey;

    @Value("${kamis.cert-id:}")
    private String certId;

    @Value("${kamis.product-cls-code:02}")
    private String defaultProductClsCode;

    @Value("${kamis.country-code:1101}")
    private String defaultCountryCode;

    @Value("${kamis.convert-kg-yn:N}")
    private String convertKgYn;

    /**
     * KAMIS 일별 부류별 시세 수집.
     * 요청한 날짜를 그대로 collected_date로 저장 (KAMIS day1 라벨은 무시).
     * 같은 (item_code, kind_code, rank_code, product_cls_code, collected_date) 조합이 이미 있으면 스킵.
     */
    @Transactional
    public PriceCollectResult collectAndSave(String categoryCode, String productClsCode, String countryCode) {
        if (certKey == null || certKey.isBlank()) {
            throw new CustomException(ErrorCode.CRAWLING_ERROR);
        }

        String resolvedProductClsCode = (productClsCode != null && !productClsCode.isBlank())
                ? productClsCode : defaultProductClsCode;
        String resolvedCountryCode = (countryCode != null && !countryCode.isBlank())
                ? countryCode : defaultCountryCode;

        LocalDate today = LocalDate.now();
        String regday = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("action", "dailyPriceByCategoryList")
                .queryParam("p_cert_key", certKey)
                .queryParam("p_cert_id", certId)
                .queryParam("p_returntype", "json")
                .queryParam("p_product_cls_code", resolvedProductClsCode)
                .queryParam("p_item_category_code", categoryCode)
                .queryParam("p_country_code", resolvedCountryCode)
                .queryParam("p_regday", regday)
                .queryParam("p_convert_kg_yn", convertKgYn)
                .build(true)
                .toUriString();

        log.info("KAMIS 시세 수집 - 부류코드: {}, 구분: {}, 지역: {}, 날짜: {}",
                categoryCode, resolvedProductClsCode, resolvedCountryCode, regday);

        JsonNode response;
        try {
            String raw = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .accept(MediaType.ALL)
                    .header("User-Agent", "Mozilla/5.0")  // KAMIS 봇 차단 회피
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.debug("KAMIS 응답 원문: {}", raw);
            response = OBJECT_MAPPER.readTree(raw);
        } catch (Exception e) {
            log.error("KAMIS API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.CRAWLING_ERROR);
        }

        if (response == null || !response.has("data")) {
            log.warn("KAMIS 응답 없음 또는 data 필드 없음");
            return new PriceCollectResult(0);
        }

        JsonNode dataNode = response.get("data");
        if (!dataNode.has("item")) {
            log.warn("KAMIS data에 item 없음 (데이터 없거나 오류): {}", dataNode);
            return new PriceCollectResult(0);
        }

        int savedCount = 0;
        for (JsonNode item : dataNode.get("item")) {
            String itemName = getText(item, "item_name");
            if (itemName == null || itemName.isBlank()) continue;

            String itemCode = getText(item, "item_code");
            String kindCode = getText(item, "kind_code");
            String rankCode = getText(item, "rank_code");

            // 중복 체크 (같은 날 같은 품목+품종+등급+구분 조합)
            Optional<MarketPrice> existing = marketPriceRepository
                    .findByItemCodeAndKindCodeAndRankCodeAndProductClsCodeAndCollectedDate(
                            itemCode, kindCode, rankCode, resolvedProductClsCode, today);
            if (existing.isPresent()) {
                continue;
            }

            MarketPrice marketPrice = MarketPrice.builder()
                    .itemName(itemName)
                    .itemCode(itemCode)
                    .kindName(getText(item, "kind_name"))
                    .kindCode(kindCode)
                    .rank(getText(item, "rank"))
                    .rankCode(rankCode)
                    .unit(getText(item, "unit"))
                    .categoryCode(categoryCode)
                    .productClsCode(resolvedProductClsCode)
                    .collectedDate(today)
                    .priceToday(parsePrice(getText(item, "dpr1")))
                    .price1dAgo(parsePrice(getText(item, "dpr2")))
                    .price1wAgo(parsePrice(getText(item, "dpr3")))
                    .price2wAgo(parsePrice(getText(item, "dpr4")))
                    .price1mAgo(parsePrice(getText(item, "dpr5")))
                    .price1yAgo(parsePrice(getText(item, "dpr6")))
                    .priceAvgYear(parsePrice(getText(item, "dpr7")))
                    .build();
            marketPriceRepository.save(marketPrice);
            savedCount++;
        }

        log.info("KAMIS 수집 완료 - 부류코드: {}, 저장: {}건", categoryCode, savedCount);
        return new PriceCollectResult(savedCount);
    }

    /**
     * 매일 오전 9시 6개 부류 자동 수집.
     */
    @Scheduled(cron = "${kamis.collect.cron:0 0 9 * * *}")
    public void scheduledCollect() {
        int total = 0;
        for (String categoryCode : CATEGORY_CODES) {
            try {
                PriceCollectResult result = collectAndSave(categoryCode, defaultProductClsCode, defaultCountryCode);
                total += result.getSavedCount();
            } catch (Exception e) {
                log.error("KAMIS 스케줄 수집 실패 - 부류코드: {}", categoryCode, e);
            }
        }
        log.info("KAMIS 스케줄 수집 완료. 총 저장: {}건", total);
    }

    @Transactional(readOnly = true)
    public MarketPriceResponse getLatest(String itemName) {
        MarketPrice marketPrice = marketPriceRepository
                .findTopByItemNameOrderByCollectedDateDesc(itemName)
                .orElseThrow(() -> new CustomException(ErrorCode.PRICE_HISTORY_NOT_FOUND));
        return MarketPriceResponse.from(marketPrice);
    }

    @Transactional(readOnly = true)
    public List<MarketPriceResponse> getRecentList() {
        return marketPriceRepository.findTop100ByOrderByCollectedDateDesc()
                .stream()
                .map(MarketPriceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MarketPriceResponse> getList(String categoryCode, String itemName) {
        boolean hasCategory = categoryCode != null && !categoryCode.isBlank();
        boolean hasItemName = itemName != null && !itemName.isBlank();

        if (hasCategory && hasItemName) {
            return marketPriceRepository
                    .findTop100ByCategoryCodeAndItemNameContainingOrderByCollectedDateDesc(categoryCode, itemName)
                    .stream().map(MarketPriceResponse::from).toList();
        } else if (hasCategory) {
            return marketPriceRepository
                    .findTop100ByCategoryCodeOrderByCollectedDateDesc(categoryCode)
                    .stream().map(MarketPriceResponse::from).toList();
        } else if (hasItemName) {
            return marketPriceRepository
                    .findTop100ByItemNameContainingOrderByCollectedDateDesc(itemName)
                    .stream().map(MarketPriceResponse::from).toList();
        } else {
            return marketPriceRepository.findTop100ByOrderByCollectedDateDesc()
                    .stream().map(MarketPriceResponse::from).toList();
        }
    }

    // ── helpers ──────────────────────────

    private String getText(JsonNode node, String key) {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) return null;
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    /** "8,200" → 8200, "-" → null, "" → null */
    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("-")) return null;
        try {
            return new BigDecimal(raw.replace(",", "").replace("원", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
