package com.example.plus.domain.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RefundRequest(
        @Size(max = 200, message = "환불 사유는 200자 이내여야 합니다.")
        String reason,

        List<@Valid RefundItemRequest> items
) {
}
