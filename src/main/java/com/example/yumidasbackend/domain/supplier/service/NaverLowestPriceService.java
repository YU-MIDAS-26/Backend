package com.example.yumidasbackend.domain.supplier.service;

import com.example.yumidasbackend.domain.supplier.dto.NaverLowestPriceItemResponse;
import com.example.yumidasbackend.domain.supplier.dto.NaverLowestPriceResponse;
import com.example.yumidasbackend.global.external.naver.NaverShoppingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NaverLowestPriceService {

    private final NaverShoppingClient naverShoppingClient;

    @Transactional(readOnly = true)
    public NaverLowestPriceResponse searchLowestPrice(String ingredientName, int topN) {
        int normalizedTopN = Math.min(Math.max(topN, 1), 10);

        List<NaverLowestPriceItemResponse> items = naverShoppingClient
                .searchLowestPrice(ingredientName, normalizedTopN)
                .stream()
                .map(NaverLowestPriceItemResponse::from)
                .toList();

        return NaverLowestPriceResponse.builder()
                .ingredientName(ingredientName)
                .topN(normalizedTopN)
                .items(items)
                .build();
    }
}
