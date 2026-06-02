package com.bsight.springserver.domain.chat.service;

import com.bsight.springserver.domain.payment.dto.ChannelBreakdownDto;
import com.bsight.springserver.domain.payment.dto.DailyStatsDto;
import com.bsight.springserver.domain.payment.dto.HourlyHeatmapDto;
import com.bsight.springserver.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 사용자 메시지에서 키워드를 감지해 관련 백엔드 데이터를 가져와
 * GPT에 전달할 컨텍스트 문자열로 조립한다.
 * (MVP — 키워드 매칭. 추후 OpenAI function calling으로 고도화 가능)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContextProvider {

    private final PaymentService paymentService;

    /**
     * @return [컨텍스트 문자열, 사용된 데이터 소스 리스트]
     */
    public Result build(String userMessage) {
        String msg = userMessage.toLowerCase(Locale.KOREAN);
        StringBuilder context = new StringBuilder();
        List<String> sources = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(30);

        // ── 일별 매출 ──────────────────────────
        if (containsAny(msg, "매출", "수익", "오늘", "이번달", "어제", "지난주")) {
            try {
                List<DailyStatsDto> daily = paymentService.getDailyStats(from, today);
                if (!daily.isEmpty()) {
                    context.append("\n[최근 30일 일별 매출 (단위: 원)]\n");
                    long total = 0;
                    long totalCount = 0;
                    for (DailyStatsDto d : daily) {
                        context.append(String.format("- %s: %,d원 (%d건)\n",
                                d.getDate(), d.getAmount(), d.getCount()));
                        total += d.getAmount();
                        totalCount += d.getCount();
                    }
                    context.append(String.format("총합: %,d원 (%d건)\n", total, totalCount));
                    sources.add("payments/stats/daily");
                }
            } catch (Exception e) {
                log.warn("daily stats 조회 실패: {}", e.getMessage());
            }
        }

        // ── 시간대/요일 히트맵 ─────────────────
        if (containsAny(msg, "시간대", "피크", "언제", "바쁜", "한가", "요일")) {
            try {
                List<HourlyHeatmapDto> heatmap = paymentService.getHourlyHeatmap(from, today);
                if (!heatmap.isEmpty()) {
                    // top 5만 컨텍스트로
                    context.append("\n[최근 30일 요일×시간대 매출 TOP 10]\n");
                    heatmap.stream()
                            .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                            .limit(10)
                            .forEach(h -> context.append(String.format(
                                    "- %s요일 %d시: %,d원 (%d건)\n",
                                    dayName(h.getDayOfWeek()), h.getHour(),
                                    h.getAmount(), h.getCount())));
                    sources.add("payments/stats/hourly-heatmap");
                }
            } catch (Exception e) {
                log.warn("heatmap 조회 실패: {}", e.getMessage());
            }
        }

        // ── 채널 비중 ──────────────────────────
        if (containsAny(msg, "매장", "배달", "비율", "비중", "채널")) {
            try {
                List<ChannelBreakdownDto> channels = paymentService.getChannelBreakdown(from, today);
                if (!channels.isEmpty()) {
                    context.append("\n[최근 30일 채널별 매출 비중]\n");
                    channels.forEach(c -> context.append(String.format(
                            "- %s: %,d원 (%d건, %.1f%%)\n",
                            c.getLabel(), c.getAmount(), c.getCount(), c.getRatio() * 100)));
                    sources.add("payments/stats/channel-breakdown");
                }
            } catch (Exception e) {
                log.warn("channel breakdown 조회 실패: {}", e.getMessage());
            }
        }

        return new Result(context.toString(), sources);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private String dayName(int dow) {
        return switch (dow) {
            case 1 -> "월"; case 2 -> "화"; case 3 -> "수";
            case 4 -> "목"; case 5 -> "금"; case 6 -> "토";
            case 7 -> "일";
            default -> "?";
        };
    }

    public record Result(String context, List<String> sources) {}
}
