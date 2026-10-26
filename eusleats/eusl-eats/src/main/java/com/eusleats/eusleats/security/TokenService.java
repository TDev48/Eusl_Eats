package com.eusleats.eusleats.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Creates and verifies signed login tokens (HMAC-SHA256).
 * Token format: base64url(role:refId:expiryEpochSeconds) + "." + base64url(signature)
 *
 * role   = STUDENT, STORE or ADMIN
 * refId  = studentId / storeId / userId (for admin)
 */
@Component
public class TokenService {

    public record Claims(String role, Integer refId, long expiresAt) {
    }

    private final byte[] secret;
    private final long ttlSeconds;

    public TokenService(
            @Value("${app.security.secret}") String secret,
            @Value("${app.security.token-hours:8}") long tokenHours) {

        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = tokenHours * 3600;
    }

    public String create(String role, Integer refId) {

        long expiresAt = System.currentTimeMillis() / 1000 + ttlSeconds;
        String payload = role + ":" + refId + ":" + expiresAt;

        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        return encodedPayload + "." + sign(encodedPayload);
    }

    /** Returns the claims, or null if the token is missing, forged or expired. */
    public Claims verify(String token) {

        try {
            if (token == null) {
                return null;
            }

            int dot = token.indexOf('.');

            if (dot <= 0) {
                return null;
            }

            String encodedPayload = token.substring(0, dot);
            String signature = token.substring(dot + 1);

            byte[] expected = sign(encodedPayload).getBytes(StandardCharsets.UTF_8);
            byte[] actual = signature.getBytes(StandardCharsets.UTF_8);

            if (!MessageDigest.isEqual(expected, actual)) {
                return null;
            }

            String payload = new String(
                    Base64.getUrlDecoder().decode(encodedPayload),
                    StandardCharsets.UTF_8);

            String[] parts = payload.split(":");

            if (parts.length != 3) {
                return null;
            }

            long expiresAt = Long.parseLong(parts[2]);

            if (expiresAt < System.currentTimeMillis() / 1000) {
                return null;
            }

            return new Claims(parts[0], Integer.valueOf(parts[1]), expiresAt);

        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String value) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));

        } catch (Exception e) {
            throw new IllegalStateException("Could not sign token", e);
        }
    }
}
