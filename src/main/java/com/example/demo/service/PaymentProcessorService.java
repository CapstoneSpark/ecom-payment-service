package com.example.demo.service;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;

public interface PaymentProcessorService {
    PaymentResponse createPaymentOrder(PaymentRequest req) throws Exception;
    void handleWebhook(String payload, String signature) throws Exception;
}
