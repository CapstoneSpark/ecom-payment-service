package com.example.demo.gateway;

import java.util.Map;

public class GatewayOrderResponse {
    private final String razorpayOrderId;
    private final Map<String, Object> raw;

    public GatewayOrderResponse(String razorpayOrderId, Map<String, Object> raw) {
        this.razorpayOrderId = razorpayOrderId;
        this.raw = raw;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public Map<String, Object> getRaw() { return raw; }
}
