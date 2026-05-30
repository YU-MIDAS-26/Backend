package com.bsight.springserver.domain.price.controller;

import com.bsight.springserver.domain.price.dto.MarketPriceResponse;
import com.bsight.springserver.domain.price.dto.PriceCollectResult;
import com.bsight.springserver.domain.price.service.MarketPriceService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Prices", description = "KAMIS 농산물 시세 API")
@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class MarketPriceController {

    private final MarketPriceService marketPriceService;

    /**
     * KAMIS 시세 수동 수집.
     * - categoryCode: 부류코드 (100:식량작물, 200:채소류, 300:특용작물, 400:과일류, 500:축산물, 600:수산물)
     * - productClsCode: 01:소매, 02:도매 (미입력 시 설정값 사용)
     * - countryCode: 지역코드 (미입력 시 설정값 사용)
     */
    @Operation(summary = "KAMIS 시세 수집", description = "지정한 부류의 일별 시세를 KAMIS에서 가져와 DB에 저장합니다.")
    @PostMapping("/collect")
    public ApiResponse<PriceCollectResult> collect(
            @RequestParam(defaultValue = "200") String categoryCode,
            @RequestParam(required = false) String productClsCode,
            @RequestParam(required = false) String countryCode
    ) {
        return ApiResponse.success("시세 수집이 완료되었습니다.",
                marketPriceService.collectAndSave(categoryCode, productClsCode, countryCode));
    }

    @Operation(summary = "특정 품목 최신 시세", description = "품목명 기준 가장 최근 수집된 시세 1건을 반환합니다.")
    @GetMapping("/latest")
    public ApiResponse<MarketPriceResponse> latest(@RequestParam String itemName) {
        return ApiResponse.success(marketPriceService.getLatest(itemName));
    }

    @Operation(summary = "최근 수집 시세 100건", description = "전체 품목의 최근 수집 시세 목록.")
    @GetMapping("/recent")
    public ApiResponse<List<MarketPriceResponse>> recent() {
        return ApiResponse.success(marketPriceService.getRecentList());
    }

    /**
     * 부류코드/품목명 필터링 조회.
     * - categoryCode: 100~600 (선택)
     * - itemName: 부분 검색 (선택)
     * - 둘 다 비우면 최근 100건 반환
     */
    @Operation(summary = "시세 목록 조회", description = "부류코드/품목명으로 필터링한 최근 100건.")
    @GetMapping("/list")
    public ApiResponse<List<MarketPriceResponse>> list(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String itemName
    ) {
        return ApiResponse.success(marketPriceService.getList(categoryCode, itemName));
    }
}
