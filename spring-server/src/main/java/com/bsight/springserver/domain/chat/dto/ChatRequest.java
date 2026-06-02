package com.bsight.springserver.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRequest {
    @NotBlank(message = "메시지를 입력해주세요.")
    private String message;
}
