package com.example.plus.domain.payment.repository;

import com.example.plus.domain.payment.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {

    @Query("SELECT COALESCE(SUM(ri.quantity), 0) FROM RefundItem ri WHERE ri.orderItem.id = :orderItemId AND ri.refund.status = 'SUCCEED'")
    Long sumRefundQuantity(@Param("orderItemId") Long orderItemId);
}
