package com.workspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}
