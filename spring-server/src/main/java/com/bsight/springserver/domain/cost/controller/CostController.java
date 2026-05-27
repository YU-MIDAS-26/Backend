package com.bsight.springserver.domain.cost.controller;

import com.bsight.springserver.domain.cost.dto.request.FixedCostRequest;
import com.bsight.springserver.domain.cost.dto.request.VariableCostRequest;
import com.bsight.springserver.domain.cost.service.CostService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Costs", description = "지출(고정비/변동비) 관리 API")
@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
public class CostController {

    private final CostService costService;

    @Operation(summary = "고정비 등록", description = "특정 년/월의 고정비(임대료, 공과금)를 등록합니다. 이미 데이터가 존재하면 수정됩니다.")
    @PostMapping("/fixed")
    public ApiResponse<Long> saveFixedCost(@Valid @RequestBody FixedCostRequest request) {
        Long costId = costService.saveOrUpdateFixedCost(request);
        return ApiResponse.success("고정비가 성공적으로 저장되었습니다.", costId);
    }

    @Operation(summary = "변동비 등록", description = "특정 날짜의 변동비(재료비, 급여)를 등록합니다.")
    @PostMapping("/variable")
    public ApiResponse<Long> createVariableCost(@Valid @RequestBody VariableCostRequest request) {
        Long costId = costService.createVariableCost(request);
        return ApiResponse.success("변동비가 성공적으로 등록되었습니다.", costId);
    }
}
