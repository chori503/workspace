package com.workspace.security.jwt;

import com.workspace.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;
    private String privateKey;
    private String publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        privateKey = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";
        publicKey = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----";

        JwtProperties properties = new JwtProperties(privateKey, publicKey, 60);
        jwtService = new JwtService(new JwtKeyProvider(properties), properties);
    }

    @Test
    void generateAndValidateToken() {
        String token = jwtService.generateToken("user@example.com", "ADMIN");
        assertNotNull(token);

        Claims claims = jwtService.validateAndParse(token);
        assertEquals("user@example.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void expiredTokenThrowsException() {
        JwtProperties expiredProperties = new JwtProperties(privateKey, publicKey, -1);
        JwtService expiredService = new JwtService(new JwtKeyProvider(expiredProperties), expiredProperties);
        String token = expiredService.generateToken("user@example.com", "USER");

        assertThrows(ExpiredJwtException.class, () -> jwtService.validateAndParse(token));
    }
}
