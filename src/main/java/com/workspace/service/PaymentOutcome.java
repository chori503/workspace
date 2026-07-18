package com.workspace.service;

public record PaymentOutcome(
        PaymentResult result,
        String reference,
        String reason
) {
}
