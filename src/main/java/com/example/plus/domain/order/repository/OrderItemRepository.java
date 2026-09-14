package com.example.plus.domain.order.repository;

import com.example.plus.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 특정 주문에 포함된 주문 상품들을 조회한다.
    List<OrderItem> findAllByOrderId(Long orderId);
}