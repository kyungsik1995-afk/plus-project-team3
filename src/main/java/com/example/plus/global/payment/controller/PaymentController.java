package com.example.plus.global.payment.controller;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getPaymentById(
            @AuthenticationPrincipal Long customerId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPayment(customerId, id)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> confirmPayment(
            @AuthenticationPrincipal Long customerId,
            @Valid @RequestBody PaymentConfirmRequest confirmRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok(paymentFacade.paymentConfirm(customerId, confirmRequest)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelPayment(
            @AuthenticationPrincipal Long customerId,
            @PathVariable Long id,
            @Valid @RequestBody PaymentCancelRequest cancelRequest
    ) {
        return ResponseEntity.ok(ApiResponse.ok(paymentFacade.paymentCancel(customerId, id, cancelRequest)));
    }
}
