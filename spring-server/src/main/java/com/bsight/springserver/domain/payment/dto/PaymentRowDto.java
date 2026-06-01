package com.bsight.springserver.domain.payment.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 판매전표 CSV의 한 행을 매핑하는 DTO
 * OpenCSV가 자동으로 한글 헤더 → 필드에 채워줌
 */
@Getter
@Setter
@NoArgsConstructor
public class PaymentRowDto {

    @CsvBindByName(column = "결제시각")
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

    @CsvBindByName(column = "주문채널")
    private String channelRaw;

    @CsvBindByName(column = "주문번호")
    private String orderNumber;

    @CsvBindByName(column = "결제금액")
    private Long amount;

    @CsvBindByName(column = "결제상태")
    private String statusRaw;       // "완료" 외에는 모두 skip
}
