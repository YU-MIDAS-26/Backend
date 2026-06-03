package com.bsight.springserver.domain.payment.repository;

import com.bsight.springserver.domain.payment.entity.Channel;
import com.bsight.springserver.domain.payment.entity.Payment;
import com.bsight.springserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByUserAndPaidAtAndOrderNumberAndChannel(
            User user, LocalDateTime paidAt, String orderNumber, Channel channel);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM payments " +
            "WHERE user_id = :userId AND paid_at BETWEEN :from AND :to", nativeQuery = true)
    Long sumAmountByUserAndPaidAtBetween(@Param("userId") Long userId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

    @Query(value = "SELECT DATE(paid_at) AS d, SUM(amount) AS total, COUNT(*) AS cnt FROM payments " +
            "WHERE user_id = :userId AND paid_at BETWEEN :from AND :to " +
            "GROUP BY DATE(paid_at) ORDER BY DATE(paid_at)", nativeQuery = true)
    List<Object[]> findDailyStatsRaw(@Param("userId") Long userId,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    @Query(value = "SELECT DAYOFWEEK(paid_at) AS dow, HOUR(paid_at) AS hr, " +
            "SUM(amount) AS total, COUNT(*) AS cnt FROM payments " +
            "WHERE user_id = :userId AND paid_at BETWEEN :from AND :to " +
            "GROUP BY DAYOFWEEK(paid_at), HOUR(paid_at) ORDER BY dow, hr", nativeQuery = true)
    List<Object[]> findHeatmapStatsRaw(@Param("userId") Long userId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    @Query(value = "SELECT channel, SUM(amount) AS total, COUNT(*) AS cnt FROM payments " +
            "WHERE user_id = :userId AND paid_at BETWEEN :from AND :to " +
            "GROUP BY channel ORDER BY channel", nativeQuery = true)
    List<Object[]> findChannelBreakdownRaw(@Param("userId") Long userId,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
}
