package com.example.yumidasbackend.domain.price.controller;

import com.example.yumidasbackend.domain.price.dto.MarketPriceResponse;
import com.example.yumidasbackend.domain.price.dto.PriceCollectResult;
import com.example.yumidasbackend.domain.price.service.MarketPriceService;
import com.example.yumidasbackend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class MarketPriceController {

    private final MarketPriceService marketPriceService;

    /**
     * KAMIS 시세 수집
     * categoryCode: 부류코드 (100:식량작물, 200:채소류, 300:특용작물, 400:과일류, 500:축산물, 600:수산물)
     * productClsCode: 01:소매, 02:도매 (미입력 시 설정값 사용)
     * countryCode: 지역코드 (미입력 시 설정값 사용)
     */
    @PostMapping("/collect")
    public ApiResponse<PriceCollectResult> collect(
            @RequestParam(defaultValue = "200") String categoryCode,
            @RequestParam(required = false) String productClsCode,
            @RequestParam(required = false) String countryCode
    ) {
        return ApiResponse.success("시세 수집이 완료되었습니다.", marketPriceService.collectAndSave(categoryCode, productClsCode, countryCode));
    }

    @GetMapping("/latest")
    public ApiResponse<MarketPriceResponse> latest(@RequestParam String itemName) {
        return ApiResponse.success(marketPriceService.getLatest(itemName));
    }

    @GetMapping("/best")
    public ApiResponse<MarketPriceResponse> best(@RequestParam String itemName) {
        return ApiResponse.success(marketPriceService.getBestPrice(itemName));
    }

    @GetMapping("/recent")
    public ApiResponse<List<MarketPriceResponse>> recent() {
        return ApiResponse.success(marketPriceService.getRecentList());
    }

    /**
     * 부류코드/품목명으로 필터링 조회
     * categoryCode: 100:식량작물, 200:채소류, 300:특용작물, 400:과일류, 500:축산물, 600:수산물
     * itemName: 품목명 부분 검색 (예: 양파, 배추)
     */
    @GetMapping("/list")
    public ApiResponse<List<MarketPriceResponse>> list(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String itemName
    ) {
        return ApiResponse.success(marketPriceService.getList(categoryCode, itemName));
    }
}
