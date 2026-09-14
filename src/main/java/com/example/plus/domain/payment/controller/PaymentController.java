package com.example.plus.domain.payment.controller;

import com.example.plus.domain.payment.dto.PaymentConfirmRequest;
import com.example.plus.domain.payment.dto.RefundRequest;
import com.example.plus.domain.payment.facade.PaymentFacade;
import com.example.plus.domain.payment.service.PaymentService;
import com.example.plus.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentFacade paymentFacade;

    // 특정 결제 정보를 조회한다.
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getPaymentById(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long id
    )
    {
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getPayment(memberId, id))
        );
    }

    // 결제를 승인한다.
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> confirmPayment(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PaymentConfirmRequest confirmRequest
    )
    {
        return ResponseEntity.ok(
                ApiResponse.success(paymentFacade.paymentConfirm(memberId, confirmRequest))
        );
    }

    // 결제를 환불한다.
    // request의 items가 없으면 전체 환불,
    // items가 있으면 부분 환불을 처리한다.
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<?>> refundPayment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RefundRequest refundRequest
    )
    {
        return ResponseEntity.ok(
                ApiResponse.success(paymentFacade.paymentRefund(memberId, id, refundRequest))
        );
    }
}