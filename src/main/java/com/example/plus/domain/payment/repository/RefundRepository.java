package com.example.plus.domain.payment.repository;

import com.example.plus.domain.payment.entity.Refund;
import com.example.plus.domain.payment.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = :status")
    Long sumRefundAmount(@Param("paymentId") Long paymentId, @Param("status") RefundStatus status);
}
