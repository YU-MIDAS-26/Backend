package com.example.yumidasbackend.domain.price.service;

import com.example.yumidasbackend.domain.price.dto.MarketPriceResponse;
import com.example.yumidasbackend.domain.price.dto.PriceCollectResult;
import com.example.yumidasbackend.domain.price.entity.MarketPrice;
import com.example.yumidasbackend.domain.price.repository.MarketPriceRepository;
import com.example.yumidasbackend.global.common.exception.CustomException;
import com.example.yumidasbackend.global.common.exception.ErrorCode;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketPriceService {

    private static final String[] CATEGORY_CODES = {"100", "200", "300", "400", "500", "600"};
    private static final DateTimeFormatter KAMIS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WebClient.Builder webClientBuilder;
    private final MarketPriceRepository marketPriceRepository;

    @Value("${kamis.base-url:http://www.kamis.or.kr/service/price/xml.do}")
    private String baseUrl;

    @Value("${kamis.cert-key:}")
    private String certKey;

    @Value("${kamis.cert-id:}")
    private String certId;

    @Value("${kamis.product-cls-code:02}")
    private String defaultProductClsCode;

    @Value("${kamis.country-code:2200}")
    private String defaultCountryCode;

    @Value("${kamis.convert-kg-yn:N}")
    private String convertKgYn;

    @Transactional
    public PriceCollectResult collectAndSave(String categoryCode, String productClsCode, String countryCode) {
        if (certKey == null || certKey.isBlank()) {
            throw new CustomException(ErrorCode.CRAWLING_ERROR);
        }

        String resolvedProductClsCode = (productClsCode != null && !productClsCode.isBlank()) ? productClsCode : defaultProductClsCode;
        String resolvedCountryCode    = (countryCode != null && !countryCode.isBlank())        ? countryCode    : defaultCountryCode;

        String regday = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

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

        log.info("KAMIS 시세 수집 - 부류코드: {}, 구분: {}, 지역: {}", categoryCode, resolvedProductClsCode, resolvedCountryCode);

        JsonNode response;
        try {
            String raw = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .accept(MediaType.ALL)
                    .header("User-Agent", "Mozilla/5.0")
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

        List<MarketPrice> collected = new ArrayList<>();
        for (JsonNode item : dataNode.get("item")) {
            String itemName = getText(item, "item_name");
            if (itemName == null || itemName.isBlank()) continue;

            BigDecimal currentPrice = parsePrice(getText(item, "dpr1"));
            if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal minPrice = currentPrice;
            BigDecimal maxPrice = currentPrice;
            for (String key : new String[]{"dpr2", "dpr3", "dpr4", "dpr5", "dpr6", "dpr7"}) {
                BigDecimal p = parsePrice(getText(item, key));
                if (p.compareTo(BigDecimal.ZERO) > 0) {
                    if (p.compareTo(minPrice) < 0) minPrice = p;
                    if (p.compareTo(maxPrice) > 0) maxPrice = p;
                }
            }

            MarketPrice marketPrice = MarketPrice.builder()
                    .itemName(itemName)
                    .avgPrice(currentPrice)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .unit(getText(item, "unit"))
                    .marketName(buildMarketName(getText(item, "kind_name"), getText(item, "rank")))
                    .collectedDate(parseDate(getText(item, "day1")))
                    .source("kamis")
                    .categoryCode(categoryCode)
                    .build();
            collected.add(marketPrice);
        }

        marketPriceRepository.saveAll(collected);
        log.info("KAMIS 수집 완료 - 부류코드: {}, 저장: {}건", categoryCode, collected.size());
        return new PriceCollectResult(collected.size());
    }

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
                .findTopByItemNameOrderByCollectedDateDescCreatedAtDesc(itemName)
                .orElseThrow(() -> new CustomException(ErrorCode.PRICE_HISTORY_NOT_FOUND));
        return MarketPriceResponse.from(marketPrice);
    }

    @Transactional(readOnly = true)
    public MarketPriceResponse getBestPrice(String itemName) {
        MarketPrice marketPrice = marketPriceRepository
                .findTopByItemNameOrderByMinPriceAscCollectedDateDesc(itemName)
                .orElseThrow(() -> new CustomException(ErrorCode.PRICE_HISTORY_NOT_FOUND));
        return MarketPriceResponse.from(marketPrice);
    }

    @Transactional(readOnly = true)
    public List<MarketPriceResponse> getRecentList() {
        return marketPriceRepository.findTop100ByOrderByCollectedDateDescCreatedAtDesc()
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
                    .findTop100ByCategoryCodeAndItemNameContainingOrderByCollectedDateDescCreatedAtDesc(categoryCode, itemName)
                    .stream().map(MarketPriceResponse::from).toList();
        } else if (hasCategory) {
            return marketPriceRepository
                    .findTop100ByCategoryCodeOrderByCollectedDateDescCreatedAtDesc(categoryCode)
                    .stream().map(MarketPriceResponse::from).toList();
        } else if (hasItemName) {
            return marketPriceRepository
                    .findTop100ByItemNameContainingOrderByCollectedDateDescCreatedAtDesc(itemName)
                    .stream().map(MarketPriceResponse::from).toList();
        } else {
            return marketPriceRepository.findTop100ByOrderByCollectedDateDescCreatedAtDesc()
                    .stream().map(MarketPriceResponse::from).toList();
        }
    }

    private String getText(JsonNode node, String key) {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) return null;
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("-")) return BigDecimal.ZERO;
        try {
            return new BigDecimal(raw.replace(",", "").replace("원", "").trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(raw.trim(), KAMIS_DATE_FORMAT);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String buildMarketName(String kindName, String rank) {
        if (kindName == null && rank == null) return null;
        if (kindName == null) return rank;
        if (rank == null) return kindName;
        return kindName + " " + rank;
    }
}
