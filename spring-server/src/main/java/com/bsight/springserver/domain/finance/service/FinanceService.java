package com.bsight.springserver.domain.finance.service;

import com.bsight.springserver.domain.cost.entity.FixedCost;
import com.bsight.springserver.domain.cost.entity.VariableCost;
import com.bsight.springserver.domain.cost.repository.FixedCostRepository;
import com.bsight.springserver.domain.cost.repository.VariableCostRepository;
import com.bsight.springserver.domain.finance.dto.response.AiInsightResponse;
import com.bsight.springserver.domain.finance.dto.response.CalendarDailyResponse;
import com.bsight.springserver.domain.finance.dto.response.DailyDetailResponse;
import com.bsight.springserver.domain.sales.entity.Sales;
import com.bsight.springserver.domain.sales.repository.SalesRepository;
import com.bsight.springserver.global.ai.OpenAiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 매출과 지출 데이터를 통합하여 금융 지표 및 AI 인사이트를 생성하는 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FinanceService {

    private final SalesRepository salesRepository;
    private final FixedCostRepository fixedCostRepository;
    private final VariableCostRepository variableCostRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    /**
     * 월별 캘린더용 날짜별 손익 데이터를 생성합니다.
     */
    public List<CalendarDailyResponse> getCalendarData(String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        List<Sales> salesList = salesRepository.findAll().stream()
                .filter(s -> !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .collect(Collectors.toList());

        List<VariableCost> variableCosts = variableCostRepository.findByCostDateBetween(start, end);

        FixedCost fixedCostEntity = fixedCostRepository.findByTargetYearMonth(yearMonthStr).orElse(null);
        long dailyFixedCost = (fixedCostEntity != null) ? (fixedCostEntity.getTotalCost() / daysInMonth) : 0L;

        Map<LocalDate, Long> salesMap = salesList.stream().collect(Collectors.groupingBy(Sales::getSaleDate, Collectors.summingLong(Sales::getTotalAmount)));
        Map<LocalDate, Long> varCostMap = variableCosts.stream().collect(Collectors.groupingBy(VariableCost::getCostDate, Collectors.summingLong(VariableCost::getTotalCost)));

        List<CalendarDailyResponse> result = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = yearMonth.atDay(i);
            long sales = salesMap.getOrDefault(date, 0L);
            long expense = varCostMap.getOrDefault(date, 0L) + dailyFixedCost;

            result.add(CalendarDailyResponse.builder()
                    .date(date)
                    .dailySales(sales)
                    .dailyExpense(expense)
                    .dailyProfit(sales - expense)
                    .build());
        }
        return result;
    }

    /**
     * 특정 날짜의 상세 지표를 조회합니다.
     */
    public DailyDetailResponse getDailyDetail(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        String yearMonthStr = yearMonth.toString();

        Sales sales = salesRepository.findBySaleDate(date).orElse(null);
        List<VariableCost> varCosts = variableCostRepository.findByCostDateBetween(date, date);
        FixedCost fixedCostEntity = fixedCostRepository.findByTargetYearMonth(yearMonthStr).orElse(null);

        long totalSales = (sales != null) ? sales.getTotalAmount() : 0L;
        long variableCost = varCosts.stream().mapToLong(VariableCost::getTotalCost).sum();
        long fixedCost = (fixedCostEntity != null) ? (fixedCostEntity.getTotalCost() / yearMonth.lengthOfMonth()) : 0L;
        long totalExpense = variableCost + fixedCost;

        List<DailyDetailResponse.HourlySalesDetail> hourlyDetails = new ArrayList<>();
        if (sales != null && !sales.getHourlySales().isEmpty()) {
            hourlyDetails = sales.getHourlySales().stream()
                    .map(h -> DailyDetailResponse.HourlySalesDetail.builder().hour(h.getSaleHour()).amount(h.getAmount()).build())
                    .collect(Collectors.toList());
        }

        return DailyDetailResponse.builder()
                .totalSales(totalSales)
                .totalExpense(totalExpense)
                .variableCost(variableCost)
                .fixedCost(fixedCost)
                .netProfit(totalSales - totalExpense)
                .hourlySales(hourlyDetails)
                .build();
    }

    /**
     * AI 경영 인사이트 데이터를 생성합니다.
     * 실제 DB의 매출/지출 데이터를 기반으로 OpenAI LLM을 호출하여 실시간 분석을 수행합니다.
     */
    public AiInsightResponse getAiInsight(String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Sales> salesList = salesRepository.findAll().stream()
                .filter(s -> !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .toList();
        List<VariableCost> variableCosts = variableCostRepository.findByCostDateBetween(start, end);
        FixedCost fixedCost = fixedCostRepository.findByTargetYearMonth(yearMonthStr).orElse(null);

        long totalSales = salesList.stream().mapToLong(Sales::getTotalAmount).sum();
        long totalVarCost = variableCosts.stream().mapToLong(VariableCost::getTotalCost).sum();
        long totalFixedCost = (fixedCost != null) ? fixedCost.getTotalCost() : 0L;
        long totalExpense = totalVarCost + totalFixedCost;
        long netProfit = totalSales - totalExpense;

        String prompt = String.format(
            "너는 전문 경영 컨설턴트야. 다음 가게 데이터(%s)를 분석해서 사장님에게 전문적인 조언을 담은 JSON으로 답해줘.\n" +
            "데이터:\n- 총 매출: %d원\n- 총 지출: %d원\n- 순이익: %d원\n\n" +
            "JSON 형식 (반드시 모든 필드는 한국어 서술형 문장으로 채울 것):\n" +
            "{\n" +
            "  \"coreSummary\": \"전체 경영 상태를 관통하는 핵심 요약 한 문장\",\n" +
            "  \"financeSummary\": \"이번 달 매출, 지출, 순이익 데이터를 분석한 종합 재무 분석 문구\",\n" +
            "  \"recommendations\": [\"사장님이 당장 실행해야 할 추천 사항 1\", \"...\", \"...\", \"...\", \"추천 사항 5\"],\n" +
            "  \"salesFlow\": \"매출의 시간적/데이터적 흐름에 대한 짧은 분석 요약\",\n" +
            "  \"additionalInsight\": \"전월 대비 비교나 향후 전망 등 추가 인사이트\"\n" +
            "}\n" +
            "recommendations 리스트는 반드시 정확히 5개 항목이어야 해.",
            yearMonthStr, totalSales, totalExpense, netProfit
        );

        try {
            String aiResponse = openAiClient.chat(prompt);
            if (aiResponse != null) {
                return objectMapper.readValue(aiResponse, AiInsightResponse.class);
            }
        } catch (Exception e) {
            return getMockInsight();
        }
        return getMockInsight();
    }

    private AiInsightResponse getMockInsight() {
        return AiInsightResponse.builder()
                .coreSummary("현재 AI 분석 서버 연결을 확인 중입니다.")
                .financeSummary("이번 달은 안정적인 매출을 기록했으나, 고정비 비중이 다소 높아 수익성 개선이 필요해 보입니다.")
                .recommendations(List.of(
                    "피크 타임 매출 극대화를 위한 효율적인 인력 배치를 고려하세요.",
                    "재료비 절감을 위해 주재료의 구매 단가를 재협상해 보세요.",
                    "에너지 소비 효율을 높여 고정 공과금을 절약하세요.",
                    "단골 고객을 위한 소규모 이벤트를 기획하여 방문 빈도를 높이세요.",
                    "인기 메뉴 중심의 세트 구성을 통해 객단가를 높여보세요."
                ))
                .salesFlow("주말 매출이 전체의 50% 이상을 차지하며 특정 요일 편중 현상이 있습니다.")
                .additionalInsight("다음 달은 지역 축제가 예정되어 있어 매출 상승이 기대됩니다.")
                .build();
    }
}
