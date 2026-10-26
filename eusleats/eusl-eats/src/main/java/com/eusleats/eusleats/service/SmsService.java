package com.eusleats.eusleats.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Sends text messages (SMS) to a phone number.
 *
 * app.sms.provider decides how:
 *   log      - only writes the message to the console (default, for testing)
 *   notifylk - Notify.lk (Sri Lankan SMS gateway)
 *   twilio   - Twilio
 *
 * Sending is done in the background and never breaks an order if it fails.
 */
@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final String provider;
    private final String countryCode;

    private final String notifyUserId;
    private final String notifyApiKey;
    private final String notifySenderId;

    private final String twilioSid;
    private final String twilioToken;
    private final String twilioFrom;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public SmsService(
            @Value("${app.sms.provider:log}") String provider,
            @Value("${app.sms.default-country-code:94}") String countryCode,
            @Value("${app.sms.notifylk.user-id:}") String notifyUserId,
            @Value("${app.sms.notifylk.api-key:}") String notifyApiKey,
            @Value("${app.sms.notifylk.sender-id:NotifyDEMO}") String notifySenderId,
            @Value("${app.sms.twilio.account-sid:}") String twilioSid,
            @Value("${app.sms.twilio.auth-token:}") String twilioToken,
            @Value("${app.sms.twilio.from:}") String twilioFrom) {

        this.provider = provider == null ? "log" : provider.trim().toLowerCase();
        this.countryCode = countryCode;
        this.notifyUserId = notifyUserId;
        this.notifyApiKey = notifyApiKey;
        this.notifySenderId = notifySenderId;
        this.twilioSid = twilioSid;
        this.twilioToken = twilioToken;
        this.twilioFrom = twilioFrom;
    }

    /** Queues a text message. Returns immediately and never throws. */
    public void send(String phone, String text) {

        try {
            if (phone == null || phone.isBlank()) {
                log.warn("SMS not sent: the store has no phone number.");
                return;
            }

            String digits = toInternational(phone);

            CompletableFuture.runAsync(() -> {
                try {
                    deliver(digits, text);
                } catch (Exception e) {
                    log.error("SMS to {} failed: {}", digits, e.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("SMS could not be queued: {}", e.getMessage());
        }
    }

    /** 0771234567 -> 94771234567 (digits only, country code first). */
    String toInternational(String phone) {

        String digits = phone.replaceAll("[^0-9]", "");

        if (phone.trim().startsWith("+")) {
            return digits;
        }

        if (digits.startsWith("00")) {
            return digits.substring(2);
        }

        if (digits.startsWith("0")) {
            return countryCode + digits.substring(1);
        }

        return digits;
    }

    private void deliver(String digits, String text) throws Exception {

        switch (provider) {

            case "notifylk" -> {
                String body = "user_id=" + enc(notifyUserId)
                        + "&api_key=" + enc(notifyApiKey)
                        + "&sender_id=" + enc(notifySenderId)
                        + "&to=" + enc(digits)
                        + "&message=" + enc(text);

                post("https://app.notify.lk/api/v1/send", body, null);
            }

            case "twilio" -> {
                String body = "To=" + enc("+" + digits)
                        + "&From=" + enc(twilioFrom)
                        + "&Body=" + enc(text);

                String auth = Base64.getEncoder().encodeToString(
                        (twilioSid + ":" + twilioToken)
                                .getBytes(StandardCharsets.UTF_8));

                post("https://api.twilio.com/2010-04-01/Accounts/"
                        + twilioSid + "/Messages.json", body, "Basic " + auth);
            }

            default -> log.info("[SMS - log mode, nothing is really sent] to +{}: {}",
                    digits, text);
        }
    }

    private void post(String url, String formBody, String authorization)
            throws Exception {

        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody));

        if (authorization != null) {
            request.header("Authorization", authorization);
        }

        HttpResponse<String> response = httpClient.send(
                request.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "gateway answered " + response.statusCode() + ": " + response.body());
        }

        log.info("SMS gateway ({}) accepted the message.", provider);
    }

    private String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
