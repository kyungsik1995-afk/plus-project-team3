package com.example.plus.domain.payment.entity;

import com.example.plus.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(length = 200)
    private String reason;

    @Column(nullable = false)
    private Long refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Column(name = "refund_at", nullable = false)
    private LocalDateTime refundAt;

    public Refund(
            Payment payment,
            Long refundAmount,
            String reason,
            LocalDateTime refundAt
    ) {
        this.payment = payment;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = RefundStatus.SUCCEED;
        this.refundAt = refundAt;
    }
}
