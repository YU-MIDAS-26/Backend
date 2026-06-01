package com.bsight.springserver.domain.payment.repository;

import com.bsight.springserver.domain.payment.entity.Channel;
import com.bsight.springserver.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByPaidAtAndOrderNumberAndChannel(
            LocalDateTime paidAt, String orderNumber, Channel channel);
}
