package com.bsight.springserver.domain.chat.controller;

import com.bsight.springserver.domain.chat.dto.ChatRequest;
import com.bsight.springserver.domain.chat.dto.ChatResponse;
import com.bsight.springserver.domain.chat.service.ChatService;
import com.bsight.springserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = "AI 챗봇 (RAG)")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "AI 챗봇 질문",
            description = "메시지를 보내면 사장님 가게 데이터(매출/히트맵/채널)를 참고해 답변합니다. " +
                          "응답은 마크다운 포맷.")
    @PostMapping
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ApiResponse.success(chatService.chat(request.getMessage()));
    }
}
