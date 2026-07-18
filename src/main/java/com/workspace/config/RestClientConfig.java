package com.workspace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient paymentRestClient(PaymentProperties paymentProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) paymentProperties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) paymentProperties.readTimeout().toMillis());

        return RestClient.builder()
                .baseUrl(paymentProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
