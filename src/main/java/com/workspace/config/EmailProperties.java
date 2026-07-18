package com.workspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(Message welcome, Message reservationConfirmed) {
    public record Message(String subject,
                          String body) {
    }
}
