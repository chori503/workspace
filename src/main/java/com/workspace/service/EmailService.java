package com.workspace.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Envío de correo interrumpido para {}", to);
            return;
        }
        log.info("Correo enviado a {} | asunto: {} | cuerpo: {}", to, subject, body);
    }
}
