package com.bsight.springserver.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 판매전표 CSV 업로드 결과
 */
@Getter
@Builder
public class UploadResult {

    private int totalRows;          // CSV 전체 데이터 행 수
    private int savedCount;         // 실제 저장된 건수
    private int skippedCount;       // 스킵된 건수 (취소/중복/검증실패 등)
    private List<RowError> errors;  // 스킵된 행의 사유 리스트

    @Getter
    @Builder
    public static class RowError {
        private int rowNumber;       // CSV 데이터 기준 행 번호 (헤더 제외, 1부터)
        private String reason;       // 사유 (한국어)
    }
}
