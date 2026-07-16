package com.workspace.security.jwt;

import com.workspace.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final JwtKeyProvider keyProvider;
    private final JwtProperties properties;

    public JwtService(JwtKeyProvider keyProvider, JwtProperties properties) {
        this.keyProvider = keyProvider;
        this.properties = properties;
    }

    public String generateToken(String subject, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.expirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(keyProvider.getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public Claims validateAndParse(String token) {
        return Jwts.parser()
                .verifyWith(keyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
