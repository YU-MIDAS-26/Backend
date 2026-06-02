package com.bsight.springserver.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 챗봇 응답
 */
@Getter
@Builder
public class ChatResponse {
    private String answer;             // GPT 응답 (마크다운 포맷)
    private List<String> usedContext;  // 어떤 백엔드 데이터 참조했는지 (예: "payments/daily", "kamis/latest")
}
