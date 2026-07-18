package com.workspace.client;

import com.workspace.dto.payment.PaymentGatewayResponse;
import com.workspace.dto.reservation.request.CardDetailsRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private final RestClient paymentRestClient;

    public PaymentClient(RestClient paymentRestClient) {
        this.paymentRestClient = paymentRestClient;
    }

    public PaymentGatewayResponse charge(CardDetailsRequest cardDetails) {
        return paymentRestClient.post()
                .uri("/api/pay")
                .body(cardDetails)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {})
                .body(PaymentGatewayResponse.class);
    }
}
