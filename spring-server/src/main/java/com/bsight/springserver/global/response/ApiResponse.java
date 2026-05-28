package com.bsight.springserver.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프론트엔드와 일관된 인터페이스를 유지하기 위한 공통 응답 규격
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "공통 응답 객체")
public class ApiResponse<T> {

    @Schema(description = "응답 상태", example = "SUCCESS")
    private String status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청 성공", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}
