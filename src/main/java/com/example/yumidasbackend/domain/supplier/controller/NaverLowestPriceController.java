package com.example.yumidasbackend.domain.supplier.controller;

import com.example.yumidasbackend.domain.supplier.dto.NaverLowestPriceRequest;
import com.example.yumidasbackend.domain.supplier.dto.NaverLowestPriceResponse;
import com.example.yumidasbackend.domain.supplier.service.NaverLowestPriceService;
import com.example.yumidasbackend.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/naver")
@RequiredArgsConstructor
public class NaverLowestPriceController {

    private final NaverLowestPriceService naverLowestPriceService;

    /**
     * 쿼리스트링에 한글을 넣을 때는 반드시 URL 인코딩 필요.
     * 예: curl -G 'http://localhost:8080/api/naver/lowest-price' --data-urlencode 'ingredientName=양파' --data-urlencode 'topN=5'
     */
    @GetMapping("/lowest-price")
    public ApiResponse<NaverLowestPriceResponse> lowestPrice(
            @RequestParam String ingredientName,
            @RequestParam(defaultValue = "5") int topN
    ) {
        return ApiResponse.success(
                naverLowestPriceService.searchLowestPrice(ingredientName, topN)
        );
    }

    /** 한글 검색어는 POST(JSON) 권장 (Tomcat이 인코딩 없는 GET 쿼리의 비ASCII를 거부할 수 있음). */
    @PostMapping("/lowest-price")
    public ApiResponse<NaverLowestPriceResponse> lowestPricePost(@Valid @RequestBody NaverLowestPriceRequest request) {
        int topN = request.getTopN() != null ? request.getTopN() : 5;
        return ApiResponse.success(
                naverLowestPriceService.searchLowestPrice(request.getIngredientName(), topN)
        );
    }
}
