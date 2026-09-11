package com.example.plus.global.payment.repository;

import com.example.plus.global.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId AND p.order.customer.id = :customerId")
    Optional<Payment> findByIdAndCustomerId(@Param("paymentId") Long paymentId, @Param("customerId") Long customerId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order o WHERE o.id = :orderId")
    Optional<Payment> findByOrderIdWithOrder(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithOrder(@Param("paymentId") Long paymentId);
}