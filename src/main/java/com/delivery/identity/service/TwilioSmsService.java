package com.delivery.identity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TwilioSmsService {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String authToken;

    @Value("${app.sms.twilio.from-number:}")
    private String fromNumber;

    public boolean isConfigured() {
        return hasText(accountSid) && hasText(authToken) && hasText(fromNumber);
    }

    public void send(String to, String body) {
        if (!isConfigured()) throw new SmsDeliveryException("O envio de SMS não está configurado.");
        String credentials = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        String form = "To=" + encode(to) + "&From=" + encode(fromNumber) + "&Body=" + encode(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SmsDeliveryException("O provedor de SMS recusou o envio (HTTP " + response.statusCode() + ").");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SmsDeliveryException("O envio do SMS foi interrompido.", exception);
        } catch (IOException exception) {
            throw new SmsDeliveryException("Não foi possível conectar ao provedor de SMS.", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static class SmsDeliveryException extends RuntimeException {
        public SmsDeliveryException(String message) { super(message); }
        public SmsDeliveryException(String message, Throwable cause) { super(message, cause); }
    }
}
