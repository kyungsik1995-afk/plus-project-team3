package com.example.plus.domain.payment.entity;

import com.example.plus.global.common.entity.BaseEntity;
import com.example.plus.orders.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    private Payment(Long finalPrice, PaymentStatus status, Order order) {
        this.finalPrice = finalPrice;
        this.status = status;
        this.order = order;
    }

    public static Payment create(Long finalPrice, PaymentStatus status, Order order) {
        return new Payment(finalPrice, status, order);
    }

    public void markAsPaid() {
        changeStatus(PaymentStatus.PAID);
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        changeStatus(PaymentStatus.FAILED);
    }

    public void markAsCancelled() {
        changeStatus(PaymentStatus.CANCELLED);
    }

    public void markAsPartCancelled() {
        changeStatus(PaymentStatus.PART_CANCELLED);
    }

    private void changeStatus(PaymentStatus nextStatus) {
        if (!this.status.canTransitTo(nextStatus)) {
            throw new IllegalStateException("유효하지 않은 상태변경");
        }
        this.status = nextStatus;
    }
}
