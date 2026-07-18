package com.workspace.dto.payment;

public record PaymentGatewayResponse(
        PaymentGatewayStatus status,
        String reference,
        String reason
) {
}
