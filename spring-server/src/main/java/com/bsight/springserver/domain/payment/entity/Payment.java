package com.bsight.springserver.domain.payment.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import com.bsight.springserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 판매전표 (결제 거래 한 건) — 사장님별 개별화
 */
@Getter
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_user_paid_at", columnList = "user_id, paidAt"),
                @Index(name = "idx_payment_user_channel_paid_at", columnList = "user_id, channel, paidAt")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_payment_user",
                columnNames = {"user_id", "paid_at", "order_number", "channel"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // 사장님

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    @Builder
    public Payment(User user, LocalDateTime paidAt, Channel channel, Long amount, String orderNumber) {
        this.user = user;
        this.paidAt = paidAt;
        this.channel = channel;
        this.amount = amount;
        this.orderNumber = orderNumber;
    }
}
