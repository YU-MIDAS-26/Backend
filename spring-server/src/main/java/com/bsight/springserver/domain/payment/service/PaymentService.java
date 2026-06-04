package com.bsight.springserver.domain.payment.service;

import com.bsight.springserver.domain.payment.dto.ChannelBreakdownDto;
import com.bsight.springserver.domain.payment.dto.DailyStatsDto;
import com.bsight.springserver.domain.payment.dto.HourlyHeatmapDto;
import com.bsight.springserver.domain.payment.dto.PaymentRowDto;
import com.bsight.springserver.domain.payment.dto.UploadResult;
import com.bsight.springserver.domain.payment.entity.Channel;
import com.bsight.springserver.domain.payment.entity.Payment;
import com.bsight.springserver.domain.payment.repository.PaymentRepository;
import com.bsight.springserver.domain.sales.service.SalesService;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.bsight.springserver.global.security.auth.CustomUserDetails;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SalesService salesService;
    private final UserRepository userRepository;

    @Transactional
    public UploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.CSV_FILE_REQUIRED);
        }

        User user = getCurrentUser();
        List<PaymentRowDto> rows = parseCsv(file);
        List<UploadResult.RowError> errors = new ArrayList<>();
        List<Payment> toSave = new ArrayList<>();
        Set<LocalDate> datesToSync = new HashSet<>();
        Set<PaymentDuplicateKey> seenPaymentKeys = new HashSet<>();

        int totalRows = rows.size();
        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 1;
            PaymentRowDto row = rows.get(i);

            if ("취소".equals(row.getStatusRaw())) {
                errors.add(rowError(rowNumber, "취소 거래는 저장하지 않음"));
                continue;
            }
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

            Channel channel = Channel.from(row.getChannelRaw());
            if (channel == null) {
                errors.add(rowError(rowNumber, "알 수 없는 주문채널: " + row.getChannelRaw()));
                continue;
            }

            PaymentDuplicateKey paymentKey = new PaymentDuplicateKey(row.getPaidAt(), row.getOrderNumber(), channel);
            if (!seenPaymentKeys.add(paymentKey)) {
                errors.add(rowError(rowNumber, "중복 거래(업로드 파일 내 중복)"));
                datesToSync.add(row.getPaidAt().toLocalDate());
                continue;
            }

            if (paymentRepository.existsByUserAndPaidAtAndOrderNumberAndChannel(
                    user, row.getPaidAt(), row.getOrderNumber(), channel)) {
                errors.add(rowError(rowNumber, "중복 거래(이미 저장됨)"));
                datesToSync.add(row.getPaidAt().toLocalDate());
                continue;
            }
            if (paymentRepository.existsByPaidAtAndOrderNumberAndChannel(
                    row.getPaidAt(), row.getOrderNumber(), channel)) {
                errors.add(rowError(rowNumber, "중복 거래(기존 데이터와 중복)"));
                datesToSync.add(row.getPaidAt().toLocalDate());
                continue;
            }

            datesToSync.add(row.getPaidAt().toLocalDate());
            toSave.add(Payment.builder()
                    .user(user)
                    .paidAt(row.getPaidAt())
                    .channel(channel)
                    .amount(row.getAmount())
                    .orderNumber(row.getOrderNumber())
                    .build());
        }

        paymentRepository.saveAll(toSave);
        syncDailySales(user, datesToSync);
        int savedCount = toSave.size();

        return UploadResult.builder()
                .totalRows(totalRows)
                .savedCount(savedCount)
                .skippedCount(totalRows - savedCount)
                .errors(errors)
                .build();
    }

    private void syncDailySales(User user, Set<LocalDate> datesToSync) {
        for (LocalDate date : datesToSync) {
            LocalDateTime fromDt = date.atStartOfDay();
            LocalDateTime toDt = date.atTime(LocalTime.MAX);
            Long totalAmount = paymentRepository.sumAmountByUserAndPaidAtBetween(user.getId(), fromDt, toDt);
            salesService.saveDailySalesFromCsv(user, date, totalAmount != null ? totalAmount : 0L);
        }
    }

    private List<PaymentRowDto> parseCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CsvToBean<PaymentRowDto> beans = new CsvToBeanBuilder<PaymentRowDto>(reader)
                    .withType(PaymentRowDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)
                    .build();
            return beans.parse();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_CSV_FORMAT);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CSV_PARSE_FAILED);
        }
    }

    private UploadResult.RowError rowError(int rowNumber, String reason) {
        return UploadResult.RowError.builder().rowNumber(rowNumber).reason(reason).build();
    }

    private record PaymentDuplicateKey(LocalDateTime paidAt, String orderNumber, Channel channel) {}

    @Transactional(readOnly = true)
    public List<DailyStatsDto> getDailyStats(LocalDate from, LocalDate to) {
        User user = getCurrentUser();
        LocalDate effectiveTo = (to != null) ? to : LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : effectiveTo.minusDays(30);

        return paymentRepository.findDailyStatsRaw(user.getId(),
                        effectiveFrom.atStartOfDay(), effectiveTo.atTime(LocalTime.MAX)).stream()
                .map(row -> DailyStatsDto.builder()
                        .date(toLocalDate(row[0]))
                        .amount(((Number) row[1]).longValue())
                        .count(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return LocalDate.parse(value.toString());
    }

    @Transactional(readOnly = true)
    public List<HourlyHeatmapDto> getHourlyHeatmap(LocalDate from, LocalDate to) {
        User user = getCurrentUser();
        LocalDate effectiveTo = (to != null) ? to : LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : effectiveTo.minusDays(30);

        return paymentRepository.findHeatmapStatsRaw(user.getId(),
                        effectiveFrom.atStartOfDay(), effectiveTo.atTime(LocalTime.MAX)).stream()
                .map(row -> HourlyHeatmapDto.builder()
                        .dayOfWeek(toKoreanDayOfWeek(((Number) row[0]).intValue()))
                        .hour(((Number) row[1]).intValue())
                        .amount(((Number) row[2]).longValue())
                        .count(((Number) row[3]).longValue())
                        .build())
                .toList();
    }

    private int toKoreanDayOfWeek(int mysqlDow) { return mysqlDow == 1 ? 7 : mysqlDow - 1; }

    @Transactional(readOnly = true)
    public List<ChannelBreakdownDto> getChannelBreakdown(LocalDate from, LocalDate to) {
        User user = getCurrentUser();
        LocalDate effectiveTo = (to != null) ? to : LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : effectiveTo.minusDays(30);

        List<Object[]> raw = paymentRepository.findChannelBreakdownRaw(user.getId(),
                effectiveFrom.atStartOfDay(), effectiveTo.atTime(LocalTime.MAX));

        long totalAmount = raw.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum();

        return raw.stream().map(row -> {
            Channel channel = Channel.valueOf((String) row[0]);
            long amount = ((Number) row[1]).longValue();
            long count = ((Number) row[2]).longValue();
            double ratio = totalAmount > 0 ? (double) amount / totalAmount : 0.0;
            return ChannelBreakdownDto.builder()
                    .channel(channel)
                    .label(channel == Channel.OFFLINE ? "매장" : "배달")
                    .amount(amount).count(count)
                    .ratio(Math.round(ratio * 10000.0) / 10000.0)
                    .build();
        }).toList();
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return userDetails.getUserId();
    }
}
