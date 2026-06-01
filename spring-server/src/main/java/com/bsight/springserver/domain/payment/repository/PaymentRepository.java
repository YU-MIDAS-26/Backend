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
}
