package com.bsight.springserver.domain.sales.controller;

import com.bsight.springserver.domain.sales.dto.request.SalesCreateRequest;
import com.bsight.springserver.domain.sales.service.SalesService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sales", description = "매출 관리 API")
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @Operation(summary = "매출 등록", description = "일별/시간별 매출 데이터를 등록합니다. 주기가 HOURLY일 경우 시간대별 목록을 포함해야 합니다.")
    @PostMapping
    public ApiResponse<Long> createSales(@Valid @RequestBody SalesCreateRequest request) {
        Long salesId = salesService.createSales(request);
        return ApiResponse.success("매출이 성공적으로 등록되었습니다.", salesId);
    }
}
