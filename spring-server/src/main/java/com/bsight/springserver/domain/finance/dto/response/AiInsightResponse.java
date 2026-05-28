package com.bsight.springserver.domain.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI가 분석한 5대 핵심 경영 인사이트 응답 DTO
 */
@Getter
@Builder
@Schema(description = "AI 경영 인사이트 응답 데이터 (5대 항목)")
public class AiInsightResponse {

    @Schema(description = "1. 한눈에 보는 핵심 요약", example = "안정적인 매출 성장세를 보이고 있습니다.")
    private String coreSummary;

    @Schema(description = "2. 당월 재무 분석 요약 문구", example = "이번 달 매출은 목표액을 상회했으나, 식자재비 상승으로 순이익은 다소 감소했습니다.")
    private String financeSummary;

    @Schema(description = "3. 잘된 점/개선할 점 등 핵심 추천 사항 (5가지)")
    private List<String> recommendations;

    @Schema(description = "4. 매출 흐름 요약", example = "주말 피크 타임에 매출이 집중되는 경향이 있습니다.")
    private String salesFlow;

    @Schema(description = "5. 월별 비교 및 추가 인사이트", example = "지난달 대비 수익성이 5% 개선되었습니다.")
    private String additionalInsight;
}
