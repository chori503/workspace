package com.workspace.security.jwt;

import com.workspace.config.JwtProperties;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtKeyProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtKeyProvider(JwtProperties properties) {
        this.privateKey = parsePrivateKey(properties.privateKey());
        this.publicKey = parsePublicKey(properties.publicKey());
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private PrivateKey parsePrivateKey(String pem) {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decode(pem));
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Llave privada RSA inválida", e);
        }
    }

    private PublicKey parsePublicKey(String pem) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decode(pem));
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Llave pública RSA inválida", e);
        }
    }

    private byte[] decode(String pem) {
        String base64 = pem
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
