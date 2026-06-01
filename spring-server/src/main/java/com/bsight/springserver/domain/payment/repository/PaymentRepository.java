package com.bsight.springserver.domain.payment.repository;

import com.bsight.springserver.domain.payment.entity.Channel;
import com.bsight.springserver.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByPaidAtAndOrderNumberAndChannel(
            LocalDateTime paidAt, String orderNumber, Channel channel);

    /**
     * 일별 매출 집계 (날짜, 금액합계, 건수)
     * 반환: Object[] = [java.sql.Date, BigDecimal/Long, Long]
     */
    @Query(value = "SELECT DATE(paid_at) AS d, SUM(amount) AS total, COUNT(*) AS cnt " +
            "FROM payments " +
            "WHERE paid_at BETWEEN :from AND :to " +
            "GROUP BY DATE(paid_at) " +
            "ORDER BY DATE(paid_at)", nativeQuery = true)
    List<Object[]> findDailyStatsRaw(@Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    /**
     * 요일 x 시간대 매출 집계 (히트맵용)
     * - DAYOFWEEK: MySQL 기본 (1=일, 2=월, ..., 7=토) — 서비스에서 한국 관습으로 변환
     * - HOUR: 0~23
     * 반환: Object[] = [Integer dow, Integer hour, BigDecimal/Long, Long]
     */
    @Query(value = "SELECT DAYOFWEEK(paid_at) AS dow, " +
            "       HOUR(paid_at)      AS hr, " +
            "       SUM(amount)        AS total, " +
            "       COUNT(*)           AS cnt " +
            "FROM payments " +
            "WHERE paid_at BETWEEN :from AND :to " +
            "GROUP BY DAYOFWEEK(paid_at), HOUR(paid_at) " +
            "ORDER BY dow, hr", nativeQuery = true)
    List<Object[]> findHeatmapStatsRaw(@Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    /**
     * 채널별 매출 집계 (도넛 차트용)
     * 반환: Object[] = [String channel, BigDecimal/Long total, Long cnt]
     */
    @Query(value = "SELECT channel, SUM(amount) AS total, COUNT(*) AS cnt " +
            "FROM payments " +
            "WHERE paid_at BETWEEN :from AND :to " +
            "GROUP BY channel " +
            "ORDER BY channel", nativeQuery = true)
    List<Object[]> findChannelBreakdownRaw(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
}
