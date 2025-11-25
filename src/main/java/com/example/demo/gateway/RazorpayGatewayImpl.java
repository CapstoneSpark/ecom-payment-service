package com.example.demo.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class RazorpayGatewayImpl implements RazorpayGateway {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String key;
    private final String secret;
    private final String webhookSecret;

    public RazorpayGatewayImpl(HttpClient httpClient,
                               ObjectMapper objectMapper,
                               @Value("${razorpay.key}") String key,
                               @Value("${razorpay.secret}") String secret,
                               @Value("${razorpay.webhookSecret}") String webhookSecret) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.key = key;
        this.secret = secret;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public GatewayOrderResponse createOrder(String orderId, BigDecimal amount, String currency, Map<String, Object> meta) throws Exception {
        // Razorpay expects amount in paise (INR): multiply by 100
        long paise = amount.multiply(new BigDecimal(100)).longValue();

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", paise);
        payload.put("currency", currency);
        payload.put("receipt", orderId);
        payload.put("payment_capture", 1); // auto capture
        if (meta != null && !meta.isEmpty()) payload.put("notes", meta);

        String body = objectMapper.writeValueAsString(payload);

        String auth = Base64.getEncoder().encodeToString((key + ":" + secret).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.razorpay.com/v1/orders"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + auth)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String respBody = response.body();

        if (status >= 200 && status < 300) {
            Map<String,Object> map = objectMapper.readValue(respBody, Map.class);
            String razorpayOrderId = (String) map.get("id");
            return new GatewayOrderResponse(razorpayOrderId, map);
        } else {
            throw new RuntimeException("Razorpay create order failed: status=" + status + " body=" + respBody);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) throws Exception {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new RuntimeException("Webhook secret not configured");
        }
        // Compute HMAC SHA256 of payload using webhookSecret
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hashBytes = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            // Compare both hex and base64 encodings to be tolerant
            String hex = bytesToHex(hashBytes);
            String b64 = Base64.getEncoder().encodeToString(hashBytes);
            return signature.equals(hex) || signature.equals(b64);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Webhook verification error", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}
