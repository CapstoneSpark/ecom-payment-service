package com.example.demo.controller;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.service.PaymentProcessorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentProcessorService service;

    public PaymentController(PaymentProcessorService service) {
        this.service = service;
    }

    /**
     * Create Razorpay order for a given internal orderId.
     * Frontend will use the razorpayOrderId to complete payment using Razorpay checkout / UPI flow.
     */
    @CrossOrigin(origins = { "http://127.0.0.1:5500", "http://localhost:5500" })
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) throws Exception {
        PaymentResponse resp = service.createPaymentOrder(request);
        return ResponseEntity.ok(resp);
    }

}
