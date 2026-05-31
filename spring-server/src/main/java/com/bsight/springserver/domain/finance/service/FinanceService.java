package com.bsight.springserver.domain.finance.service;

import com.bsight.springserver.common.enums.CycleType;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service that aggregates sales/cost data for finance dashboard and AI insights.
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
     * Builds day-by-day calendar data for a given month.
     * - Sales: latest DAILY/HOURLY record per day
     * - Variable cost: latest DAILY record per day
     */
    public List<CalendarDailyResponse> getCalendarData(String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        List<Sales> salesList = salesRepository.findAll().stream()
                .filter(s -> !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .filter(s -> isFinanceSalesCycle(s.getCycleType()))
                .toList();

        List<VariableCost> variableCosts = variableCostRepository.findByCostDateBetween(start, end).stream()
                .filter(variableCost -> variableCost.getCycleType() == CycleType.DAILY)
                .toList();

        FixedCost fixedCostEntity = fixedCostRepository.findByTargetYearMonth(yearMonthStr).orElse(null);
        long dailyFixedCost = (fixedCostEntity != null) ? (fixedCostEntity.getTotalCost() / daysInMonth) : 0L;

        Map<LocalDate, Sales> latestSalesByDate = salesList.stream()
                .collect(Collectors.toMap(
                        Sales::getSaleDate,
                        Function.identity(),
                        this::pickLatestSales
                ));

        Map<LocalDate, VariableCost> latestVariableCostByDate = variableCosts.stream()
                .collect(Collectors.toMap(
                        VariableCost::getCostDate,
                        Function.identity(),
                        this::pickLatestVariableCost
                ));

        List<CalendarDailyResponse> result = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = yearMonth.atDay(i);
            long sales = latestSalesByDate.containsKey(date) ? latestSalesByDate.get(date).getTotalAmount() : 0L;
            long variableCost = latestVariableCostByDate.containsKey(date) ? latestVariableCostByDate.get(date).getTotalCost() : 0L;
            long expense = variableCost + dailyFixedCost;

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
     * Returns detailed finance values for a single day.
     */
    public DailyDetailResponse getDailyDetail(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        String yearMonthStr = yearMonth.toString();

        Sales sales = salesRepository.findAllBySaleDate(date).stream()
                .filter(s -> isFinanceSalesCycle(s.getCycleType()))
                .max(latestUpdatedSalesComparator())
                .orElse(null);

        VariableCost variableCostEntity = variableCostRepository.findByCostDateBetween(date, date).stream()
                .filter(variableCost -> variableCost.getCycleType() == CycleType.DAILY)
                .max(latestUpdatedVariableCostComparator())
                .orElse(null);

        FixedCost fixedCostEntity = fixedCostRepository.findByTargetYearMonth(yearMonthStr).orElse(null);

        long totalSales = (sales != null) ? sales.getTotalAmount() : 0L;
        long variableCost = (variableCostEntity != null) ? variableCostEntity.getTotalCost() : 0L;
        long fixedCost = (fixedCostEntity != null) ? (fixedCostEntity.getTotalCost() / yearMonth.lengthOfMonth()) : 0L;
        long totalExpense = variableCost + fixedCost;

        List<DailyDetailResponse.HourlySalesDetail> hourlyDetails = new ArrayList<>();
        if (sales != null && sales.getCycleType() == CycleType.HOURLY && !sales.getHourlySales().isEmpty()) {
            hourlyDetails = sales.getHourlySales().stream()
                    .sorted(Comparator.comparing(h -> h.getSaleHour()))
                    .map(h -> DailyDetailResponse.HourlySalesDetail.builder().hour(h.getSaleHour()).amount(h.getAmount()).build())
                    .toList();
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
     * Builds monthly AI insight payload using latest record per day.
     */
    public AiInsightResponse getAiInsight(String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Sales> salesList = salesRepository.findAll().stream()
                .filter(s -> !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .filter(s -> isFinanceSalesCycle(s.getCycleType()))
                .toList();

        List<VariableCost> variableCosts = variableCostRepository.findByCostDateBetween(start, end).stream()
                .filter(variableCost -> variableCost.getCycleType() == CycleType.DAILY)
                .toList();

        FixedCost fixedCost = fixedCostRepository.findByTargetYearMonth(yearMonthStr).orElse(null);

        Map<LocalDate, Sales> latestSalesByDate = salesList.stream()
                .collect(Collectors.toMap(
                        Sales::getSaleDate,
                        Function.identity(),
                        this::pickLatestSales
                ));

        Map<LocalDate, VariableCost> latestVariableCostByDate = variableCosts.stream()
                .collect(Collectors.toMap(
                        VariableCost::getCostDate,
                        Function.identity(),
                        this::pickLatestVariableCost
                ));

        long totalSales = latestSalesByDate.values().stream().mapToLong(Sales::getTotalAmount).sum();
        long totalVarCost = latestVariableCostByDate.values().stream().mapToLong(VariableCost::getTotalCost).sum();
        long totalFixedCost = (fixedCost != null) ? fixedCost.getTotalCost() : 0L;
        long totalExpense = totalVarCost + totalFixedCost;
        long netProfit = totalSales - totalExpense;

        String prompt = String.format(
                "You are a senior business consultant. Analyze the monthly store data (%s) and return only JSON.%n" +
                        "Data:%n- Total Sales: %d%n- Total Expense: %d%n- Net Profit: %d%n%n" +
                        "Required JSON schema:%n" +
                        "{%n" +
                        "  \"coreSummary\": \"One-sentence executive summary\",%n" +
                        "  \"financeSummary\": \"Financial interpretation of sales/expense/profit\",%n" +
                        "  \"recommendations\": [\"Action 1\", \"Action 2\", \"Action 3\", \"Action 4\", \"Action 5\"],%n" +
                        "  \"salesFlow\": \"Short analysis of sales trend\",%n" +
                        "  \"additionalInsight\": \"Any additional forecast or comparison\"%n" +
                        "}%n" +
                        "The recommendations array must contain exactly 5 items.",
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

    private Sales pickLatestSales(Sales left, Sales right) {
        return latestUpdatedSalesComparator().compare(left, right) >= 0 ? left : right;
    }

    private VariableCost pickLatestVariableCost(VariableCost left, VariableCost right) {
        return latestUpdatedVariableCostComparator().compare(left, right) >= 0 ? left : right;
    }

    private boolean isFinanceSalesCycle(CycleType cycleType) {
        return cycleType == CycleType.DAILY || cycleType == CycleType.HOURLY;
    }

    private Comparator<Sales> latestUpdatedSalesComparator() {
        return Comparator.comparing(Sales::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Sales::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private Comparator<VariableCost> latestUpdatedVariableCostComparator() {
        return Comparator.comparing(VariableCost::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(VariableCost::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private AiInsightResponse getMockInsight() {
        return AiInsightResponse.builder()
                .coreSummary("AI insight service fallback response.")
                .financeSummary("Stable sales trend with room to improve fixed-cost efficiency.")
                .recommendations(List.of(
                        "Optimize staffing around peak hours.",
                        "Renegotiate major ingredient purchase prices.",
                        "Reduce utility waste through operational checks.",
                        "Run retention events for repeat customers.",
                        "Promote high-margin bundle menus."
                ))
                .salesFlow("Weekend concentration appears stronger than weekdays.")
                .additionalInsight("Seasonal events next month may lift demand.")
                .build();
    }
}

