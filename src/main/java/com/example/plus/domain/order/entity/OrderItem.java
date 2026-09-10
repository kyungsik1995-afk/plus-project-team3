package com.example.plus.domain.order.entity;

import com.example.plus.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 주문에 포함된 상품인지 나타낸다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 주문한 상품을 참조한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 주문 당시의 상품명을 저장한다.
    // 상품명이 이후 변경되어도 과거 주문 내역에는 당시 상품명이 유지된다.
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    // 주문 당시의 상품 가격을 저장한다.
    // 상품 가격이 이후 변경되어도 과거 주문 금액은 변경되지 않는다.
    @Column(name = "order_price", nullable = false)
    private Long orderPrice;

    // 주문한 상품의 수량
    @Column(nullable = false)
    private Integer quantity;

    // 주문 상품 생성
    public OrderItem(
            Order order,
            Product product,
            String productName,
            Long orderPrice,
            Integer quantity
    ) {
        this.order = order;
        this.product = product;
        this.productName = productName;
        this.orderPrice = orderPrice;
        this.quantity = quantity;
    }
}