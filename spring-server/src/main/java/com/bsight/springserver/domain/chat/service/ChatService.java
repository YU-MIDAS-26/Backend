package com.bsight.springserver.domain.chat.service;

import com.bsight.springserver.domain.chat.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * RAG 챗봇 — 사용자 메시지의 키워드를 분석해 BSight 데이터(매출/히트맵/채널)를
 * 시스템 프롬프트에 컨텍스트로 주입한 뒤 OpenAI에 질문한다.
 *
 * 응답은 마크다운 포맷 (표/리스트 자유롭게 사용 가능).
 */
@Slf4j
@Service
public class ChatService {

    private static final String SYSTEM_PROMPT_BASE = """
            당신은 BSight의 소상공인 사업 비서입니다.
            사장님이 본인 가게의 매출/시간대/채널/시세 등을 질문하면 친근하고 정확하게 답하세요.

            규칙:
            - 답변은 한국어로
            - **마크다운**을 적극 활용 (표, 굵게, 리스트 등)
            - 숫자는 천 단위 콤마 포함 (예: 1,234,567원)
            - 데이터가 없으면 솔직하게 "최근 데이터가 없습니다"라고 답
            - 추측·과장 금지
            - 답변 끝에 짧은 인사이트 한 줄 추가 (예: "이번주 토요일이 가장 바빴어요!")
            """;

    private final RestClient restClient;
    private final String model;
    private final ContextProvider contextProvider;

    public ChatService(@Value("${openai.api-key}") String apiKey,
                       @Value("${openai.model}") String model,
                       ContextProvider contextProvider) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
        this.contextProvider = contextProvider;
    }

    public ChatResponse chat(String userMessage) {
        ContextProvider.Result ctx = contextProvider.build(userMessage);

        String systemPrompt = SYSTEM_PROMPT_BASE;
        if (!ctx.context().isBlank()) {
            systemPrompt += "\n\n=== 사장님 가게 실제 데이터 ===\n" + ctx.context();
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.5
            );

            @SuppressWarnings("rawtypes")
            Map response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            String answer = extractAnswer(response);
            log.info("챗봇 응답 완료 - 컨텍스트 소스: {}", ctx.sources());

            return ChatResponse.builder()
                    .answer(answer != null ? answer : "응답을 생성하지 못했어요. 잠시 후 다시 시도해주세요.")
                    .usedContext(ctx.sources())
                    .build();

        } catch (Exception e) {
            log.error("OpenAI 호출 실패: {}", e.getMessage());
            return ChatResponse.builder()
                    .answer("죄송해요, 지금은 답변을 드리기 어려워요. 잠시 후 다시 시도해주세요.")
                    .usedContext(ctx.sources())
                    .build();
        }
    }

    @SuppressWarnings("rawtypes")
    private String extractAnswer(Map response) {
        if (response == null) return null;
        Object choices = response.get("choices");
        if (!(choices instanceof List<?> list) || list.isEmpty()) return null;
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) return null;
        Object message = firstMap.get("message");
        if (!(message instanceof Map<?, ?> msgMap)) return null;
        Object content = msgMap.get("content");
        return content instanceof String s ? s : null;
    }
}
