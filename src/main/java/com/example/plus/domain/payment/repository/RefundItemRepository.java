package com.example.plus.domain.payment.repository;

import com.example.plus.domain.payment.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {

    // 특정 주문 상품에 대해 이미 환불된 수량을 조회한다.
    @Query("""
            SELECT COALESCE(SUM(ri.quantity), 0)
            FROM RefundItem ri
            WHERE ri.orderItem.id = :orderItemId
            AND ri.refund.status = 'SUCCEED'
            """)
    Long sumRefundedQuantity(@Param("orderItemId") Long orderItemId);
}