package com.example.plus.domain.payment.entity;

import com.example.plus.global.common.entity.BaseEntity;
import com.example.plus.orders.entity.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refund_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long refundAmount;

    public RefundItem(
            Refund refund,
            OrderItem orderItem,
            Integer quantity,
            Long refundAmount
    ) {
        this.refund = refund;
        this.orderItem = orderItem;
        this.quantity = quantity;
        this.refundAmount = refundAmount;
    }
}
