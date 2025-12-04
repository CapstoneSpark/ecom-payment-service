
package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.PaymentRecord;
import com.example.demo.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PaymentProcessorService {

    private final PaymentRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${razorpay.key}")
    private String razorKey;

    @Value("${razorpay.secret}")              // used ONLY for order creation
    private String razorSecret;

    @Value("${razorpay.webhookSecret}")      // used ONLY for webhook validation
    private String razorWebhookSecret;

    @Value("${order.service.url:http://localhost:8084}")
    private String orderServiceUrl;

    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl;

    public PaymentProcessorService(PaymentRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // ----------------------------------------------------------------------
    //  CREATE PAYMENT ORDER
    // ----------------------------------------------------------------------
    public PaymentResponse createPaymentOrder(PaymentRequest req) throws Exception {

        PaymentRecord rec = new PaymentRecord();
        rec.setUserId(req.getUserId());
        rec.setAmount(req.getAmount());
        rec.setStatus("CREATED");

        // Save JSON data (raw internal DTOs from frontend)
        rec.setItemsJson(objectMapper.writeValueAsString(req.getItems()));
        rec.setShippingJson(objectMapper.writeValueAsString(req.getShipping()));

        repository.save(rec);

        boolean useReal = Boolean.parseBoolean(
                System.getenv().getOrDefault("USE_REAL_RAZORPAY", "true")
        );

        String razorpayOrderId;

        if (useReal) {
            // REAL Razorpay order
            Map<String, Object> body = new HashMap<>();
            body.put("amount", req.getAmount());
            body.put("currency", req.getCurrency());
            body.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 8));

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBasicAuth(razorKey, razorSecret);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<String> entity =
                    new org.springframework.http.HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            var resp = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/orders",
                    entity,
                    String.class
            );

            Map<?, ?> respJson = objectMapper.readValue(resp.getBody(), Map.class);
            razorpayOrderId = (String) respJson.get("id");

        } else {
            // DEV mode fake orderId
            razorpayOrderId = "order_" + UUID.randomUUID().toString().substring(0, 14);
        }

        rec.setOrderId(razorpayOrderId);
        repository.save(rec);

        PaymentResponse response = new PaymentResponse();
        response.setOrderId(razorpayOrderId);
        response.setAmount(req.getAmount());
        response.setCurrency(req.getCurrency());
        response.setKey(razorKey);

        return response;
    }

    // ----------------------------------------------------------------------
    //  HANDLE WEBHOOK
    // ----------------------------------------------------------------------
    public void handleWebhook(String payload, String signatureHeader) throws Exception {

        if (signatureHeader == null)
            throw new IllegalArgumentException("Signature missing");

        log.info("Webhook: Verifying signature using webhookSecret length={}", razorWebhookSecret.length());

        // Validate signature
        if (!verifyRazorpaySignature(payload, signatureHeader, razorWebhookSecret)) {

            String computedHex = bytesToHex(hmacSha256(payload, razorWebhookSecret));
            log.error("WEBHOOK SIGNATURE FAILED: computedHex={}, header={}", computedHex, signatureHeader);

            throw new SecurityException("Invalid Razorpay signature");
        }

        Map<?, ?> json = objectMapper.readValue(payload, Map.class);
        String event = (String) json.get("event");

        if (!List.of("order.paid", "payment.captured", "payment.authorized").contains(event)) {
            log.info("Ignoring irrelevant webhook event {}", event);
            return;
        }

        Map<?, ?> payloadObj = (Map<?, ?>) json.get("payload");
        if (payloadObj == null) return;

        Map<?, ?> paymentObj = (Map<?, ?>) payloadObj.get("payment");
        if (paymentObj == null) return;

        Map<?, ?> entity = (Map<?, ?>) paymentObj.get("entity");
        if (entity == null) return;

        String orderId = (String) entity.get("order_id");
        String paymentId = (String) entity.get("id");
        String paymentStatus = (String) entity.get("status");

        if (orderId == null) {
            log.warn("Webhook missing order_id — skipping.");
            return;
        }

        Optional<PaymentRecord> opt = repository.findByOrderId(orderId);
        if (opt.isEmpty()) {
            log.warn("Webhook for unknown order {}", orderId);
            return;
        }

        PaymentRecord rec = opt.get();
        rec.setGatewayPaymentId(paymentId);

        if ("captured".equals(paymentStatus) || "authorized".equals(paymentStatus)) {
            rec.setStatus("PAID");
            repository.save(rec);

            pushOrderToOrderService(rec);
        } else {
            rec.setStatus(paymentStatus.toUpperCase());
            repository.save(rec);
        }
    }

    // ----------------------------------------------------------------------
    //  PUSH ORDER TO ORDER SERVICE
    // ----------------------------------------------------------------------
    private void pushOrderToOrderService(PaymentRecord rec) {

        try {
            OrderRequestDto dto = new OrderRequestDto();
            dto.setUserId(rec.getUserId());
            dto.setPaymentMethod("RAZORPAY");
            dto.setIdempotencyKey(rec.getOrderId());
            dto.setCartId(null); // or map cart id if available

            OrderItemDto[] itemsRaw =
                    objectMapper.readValue(rec.getItemsJson(), OrderItemDto[].class);

            List<OrderItemDto> finalItems = new ArrayList<>();

            for (OrderItemDto raw : itemsRaw) {

                Long productId = raw.getProductId();
                String sku = raw.getSku();

                // If productId is missing → lookup by SKU
                if ((productId == null || productId == 0) && sku != null && !sku.isBlank()) {

                    String lookupUrl = productServiceUrl + "/api/v1/products/sku/" + sku;

                    try {
                        ProductLookupDto product =
                                restTemplate.getForObject(lookupUrl, ProductLookupDto.class);

                        if (product == null || product.getProductId() == null) {
                            log.error("Product lookup returned NULL for SKU {}", sku);
                            rec.setStatus("PRODUCT_LOOKUP_FAILED");
                            repository.save(rec);
                            return; // stop processing gracefully
                        }

                        productId = product.getProductId();

                    } catch (HttpClientErrorException ex) {
                        log.error("HTTP error during product lookup for SKU {} -> {}", sku, ex.getStatusCode());
                        rec.setStatus("PRODUCT_LOOKUP_" + ex.getStatusCode().value());
                        repository.save(rec);
                        return; // stop gracefully, DO NOT THROW
                    } catch (Exception ex) {
                        log.error("Product lookup exception for SKU {} -> {}", sku, ex.getMessage());
                        rec.setStatus("PRODUCT_LOOKUP_FAILED");
                        repository.save(rec);
                        return;
                    }
                }

                // Build final DTO
                OrderItemDto out = new OrderItemDto();
                out.setProductId(productId);
                out.setSku(sku);
                out.setQuantity(raw.getQuantity());
                out.setUnitPrice(raw.getUnitPrice());
                out.setName(raw.getName());
                out.setImage(raw.getImage());

                // FIXED subtotal calculation (BigDecimal) — defensively compute if missing
                BigDecimal unit = raw.getUnitPrice() == null ? BigDecimal.ZERO : raw.getUnitPrice();
                Integer q = raw.getQuantity() == null ? 0 : raw.getQuantity();
                BigDecimal subtotal = (raw.getSubtotal() == null)
                        ? unit.multiply(BigDecimal.valueOf(q))
                        : raw.getSubtotal();

                out.setSubtotal(subtotal);

                finalItems.add(out);
            }

            dto.setItems(finalItems);

            // Shipping info
            ShippingDto shipping =
                    objectMapper.readValue(rec.getShippingJson(), ShippingDto.class);
            dto.setShipping(shipping);

            // Push to Order Service
            String url = orderServiceUrl + "/api/v1/orders";
            try {
                restTemplate.postForEntity(url, dto, String.class);
                rec.setStatus("ORDER_CREATED");
                repository.save(rec);
                log.info("Order pushed to Order Service successfully!");
            } catch (HttpClientErrorException httpEx) {
                log.error("OrderService push failed: {} : {}", httpEx.getStatusCode().value(), httpEx.getResponseBodyAsString());
                rec.setStatus("ORDER_PUSH_" + httpEx.getStatusCode().value());
                repository.save(rec);
            } catch (Exception ex) {
                log.error("OrderService push failed: {}", ex.getMessage());
                rec.setStatus("ORDER_PUSH_FAILED");
                repository.save(rec);
            }

        } catch (Exception ex) {
            log.error("OrderService push failed (outer): {}", ex.getMessage());
            rec.setStatus("ORDER_PUSH_FAILED");
            repository.save(rec);
        }
    }

    // ----------------------------------------------------------------------
    //  FIND PAYMENT BY ORDER ID
    // ----------------------------------------------------------------------
    public Optional<PaymentRecord> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }

    // ----------------------------------------------------------------------
    //  SIGNATURE VALIDATION
    // ----------------------------------------------------------------------
    private boolean verifyRazorpaySignature(String payload, String headerHex, String secret) {
        try {
            byte[] expected = hexToBytes(headerHex);      // Razorpay header → bytes
            byte[] computed = hmacSha256(payload, secret); // our computed HMAC bytes

            return MessageDigest.isEqual(computed, expected);

        } catch (Exception ex) {
            log.error("Signature verification exception", ex);
            return false;
        }
    }

    private byte[] hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        return mac.doFinal(data.getBytes());
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] output = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            output[i / 2] = (byte) (
                    (Character.digit(hex.charAt(i), 16) << 4) +
                            Character.digit(hex.charAt(i + 1), 16)
            );
        }
        return output;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
