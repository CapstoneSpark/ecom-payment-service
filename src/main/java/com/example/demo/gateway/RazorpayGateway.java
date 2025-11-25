package com.example.demo.gateway;

import java.math.BigDecimal;
import java.util.Map;

public interface RazorpayGateway {
    /**
     * Create an order on Razorpay and return the created order id and raw response map.
     * Amount should be provided in smallest unit (paise) as Razorpay expects integer.
     */
    GatewayOrderResponse createOrder(String orderId, BigDecimal amount, String currency, Map<String, Object> meta) throws Exception;

    /**
     * Verify webhook signature. Returns true if valid.
     * payload = raw request body
     * signature = value of header "X-Razorpay-Signature"
     */
    boolean verifyWebhookSignature(String payload, String signature) throws Exception;
}
