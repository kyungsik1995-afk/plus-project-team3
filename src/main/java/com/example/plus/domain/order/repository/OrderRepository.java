package com.example.plus.domain.order.repository;

import com.example.plus.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 데이터에 대한 DB 접근을 담당하는 Repository
 *
 * <p>Spring Data JPA의 JpaRepository를 상속하여
 * 주문의 기본적인 저장, 조회, 수정, 삭제 기능을 제공한다.</p>
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}