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
 * 판매전표 (결제 거래 한 건)
 * CSV 업로드를 통해 적재됨. 차트(일별 매출/요일x시간 히트맵/채널 비중) 집계 대상.
 * 사장님(user)별로 개별화됨.
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
    private LocalDateTime paidAt;       // 결제시각

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;            // OFFLINE / DELIVERY

    @Column(nullable = false)
    private Long amount;                // 결제금액 (원)

    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;         // 주문번호 (중복 방지용)

    @Builder
    public Payment(User user, LocalDateTime paidAt, Channel channel, Long amount, String orderNumber) {
        this.user = user;
        this.paidAt = paidAt;
        this.channel = channel;
        this.amount = amount;
        this.orderNumber = orderNumber;
    }
}
