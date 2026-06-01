package com.bsight.springserver.domain.payment.service;

import com.bsight.springserver.domain.payment.dto.DailyStatsDto;
import com.bsight.springserver.domain.payment.dto.PaymentRowDto;
import com.bsight.springserver.domain.payment.dto.UploadResult;
import com.bsight.springserver.domain.payment.entity.Channel;
import com.bsight.springserver.domain.payment.entity.Payment;
import com.bsight.springserver.domain.payment.repository.PaymentRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 판매전표 CSV 업로드 → 검증 → DB 저장
     * - 취소 거래(결제상태=취소) skip
     * - 중복 거래(paid_at + order_number + channel) skip
     * - 알 수 없는 채널 skip + 에러 리포트
     */
    @Transactional
    public UploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.CSV_FILE_REQUIRED);
        }

        List<PaymentRowDto> rows = parseCsv(file);
        List<UploadResult.RowError> errors = new ArrayList<>();
        List<Payment> toSave = new ArrayList<>();

        int totalRows = rows.size();
        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 1;     // CSV 헤더 제외, 1부터
            PaymentRowDto row = rows.get(i);

            // 1) 취소 거래 skip
            if ("취소".equals(row.getStatusRaw())) {
                errors.add(rowError(rowNumber, "취소 거래는 저장하지 않음"));
                continue;
            }

            // 2) 필수값 검증
            if (row.getPaidAt() == null) {
                errors.add(rowError(rowNumber, "결제시각이 비어있음"));
                continue;
            }
            if (row.getOrderNumber() == null || row.getOrderNumber().isBlank()) {
                errors.add(rowError(rowNumber, "주문번호가 비어있음"));
                continue;
            }
            if (row.getAmount() == null || row.getAmount() < 0) {
                errors.add(rowError(rowNumber, "결제금액이 비어있거나 음수"));
                continue;
            }

            // 3) 채널 매핑
            Channel channel = Channel.from(row.getChannelRaw());
            if (channel == null) {
                errors.add(rowError(rowNumber, "알 수 없는 주문채널: " + row.getChannelRaw()));
                continue;
            }

            // 4) 중복 체크
            if (paymentRepository.existsByPaidAtAndOrderNumberAndChannel(
                    row.getPaidAt(), row.getOrderNumber(), channel)) {
                errors.add(rowError(rowNumber, "중복 거래(이미 저장됨)"));
                continue;
            }

            // 5) 통과 → 엔티티 생성
            toSave.add(Payment.builder()
                    .paidAt(row.getPaidAt())
                    .channel(channel)
                    .amount(row.getAmount())
                    .orderNumber(row.getOrderNumber())
                    .build());
        }

        paymentRepository.saveAll(toSave);
        int savedCount = toSave.size();
        int skippedCount = totalRows - savedCount;

        log.info("판매전표 CSV 업로드 - 총 {}건, 저장 {}건, 스킵 {}건",
                totalRows, savedCount, skippedCount);

        return UploadResult.builder()
                .totalRows(totalRows)
                .savedCount(savedCount)
                .skippedCount(skippedCount)
                .errors(errors)
                .build();
    }

    private List<PaymentRowDto> parseCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CsvToBean<PaymentRowDto> beans = new CsvToBeanBuilder<PaymentRowDto>(reader)
                    .withType(PaymentRowDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)   // 잘못된 행이 있어도 전체 중단 없이 진행
                    .build();
            return beans.parse();
        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_CSV_FORMAT);
        } catch (Exception e) {
            log.error("CSV 파싱 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.CSV_PARSE_FAILED);
        }
    }

    private UploadResult.RowError rowError(int rowNumber, String reason) {
        return UploadResult.RowError.builder()
                .rowNumber(rowNumber)
                .reason(reason)
                .build();
    }

    /**
     * 일별 매출 추이 조회
     * - from/to 미입력 시 최근 30일 기본값
     * - from은 00:00:00, to는 23:59:59까지 포함
     */
    @Transactional(readOnly = true)
    public List<DailyStatsDto> getDailyStats(LocalDate from, LocalDate to) {
        LocalDate effectiveTo = (to != null) ? to : LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : effectiveTo.minusDays(30);

        LocalDateTime fromDt = effectiveFrom.atStartOfDay();
        LocalDateTime toDt = effectiveTo.atTime(LocalTime.MAX);

        return paymentRepository.findDailyStatsRaw(fromDt, toDt).stream()
                .map(row -> DailyStatsDto.builder()
                        .date(toLocalDate(row[0]))
                        .amount(((Number) row[1]).longValue())
                        .count(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    /**
     * Native query DATE() 결과를 LocalDate로 안전하게 변환
     * (드라이버/Hibernate 버전에 따라 java.sql.Date, LocalDate, String 중 하나로 반환됨)
     */
    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return LocalDate.parse(value.toString());
    }
}
