package com.workspace.service;

import com.workspace.client.PaymentClient;
import com.workspace.dto.payment.PaymentGatewayResponse;
import com.workspace.dto.payment.PaymentGatewayStatus;
import com.workspace.dto.reservation.request.CardDetailsRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentClient paymentClient;

    public PaymentService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "chargeFallback")
    public PaymentOutcome charge(CardDetailsRequest cardDetails) {
        PaymentGatewayResponse response = paymentClient.charge(cardDetails);

        if (response.status() == PaymentGatewayStatus.APPROVED) {
            return new PaymentOutcome(PaymentResult.APPROVED, response.reference(), null);
        }

        String reason = response.reason() != null ? response.reason() : "Pago rechazado por el proveedor";
        return new PaymentOutcome(PaymentResult.DECLINED, response.reference(), reason);
    }

    @SuppressWarnings("unused")
    private PaymentOutcome chargeFallback(CardDetailsRequest cardDetails, Throwable throwable) {
        log.warn("Pasarela de pago no disponible (circuito abierto o falla técnica), dejando reserva en pago pendiente: {}",
                throwable.getMessage());
        return new PaymentOutcome(PaymentResult.GATEWAY_UNAVAILABLE, null, null);
    }
}
