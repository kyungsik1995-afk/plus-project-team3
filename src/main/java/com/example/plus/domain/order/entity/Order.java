package com.example.plus.domain.order.entity;

import com.example.plus.domain.member.entity.Member;
import com.example.plus.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 주문을 구분하기 위한 고유 주문 번호
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    // 주문 시점의 상품 가격을 기준으로 계산된 총 주문 금액
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    // 현재 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // 하나의 주문은 여러 개의 주문 상품을 가질 수 있다.
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems = new ArrayList<>();
}