//package com.example.demo.controller;
//
//import com.example.demo.dto.PaymentRequest;
//import com.example.demo.dto.PaymentResponse;
//import com.example.demo.entity.PaymentRecord;
//import com.example.demo.service.PaymentProcessorService;
//import jakarta.validation.Valid;
//
//import java.util.Map;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@CrossOrigin(origins = { "http://127.0.0.1:5500", "http://localhost:5500" })
//@RequestMapping("/api/v1/payments")
//public class PaymentController {
//
//    private final PaymentProcessorService service;
//
//    public PaymentController(PaymentProcessorService service) {
//        this.service = service;
//    }
//
//    /**
//     * Create Razorpay order for a given internal orderId.
//     * Frontend will use the razorpayOrderId to complete payment using Razorpay checkout / UPI flow.
//     */
//   
//    @PostMapping("/create")
//    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) throws Exception {
//        PaymentResponse resp = service.createPaymentOrder(request);
//        return ResponseEntity.ok(resp);
//    }
//    @GetMapping("/status/{orderId}")
//    public ResponseEntity<?> getStatus(@PathVariable("orderId") String orderId) {
//
//        PaymentRecord rec = service.findByOrderId(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        Map<String, Object> resp = new java.util.HashMap<>();
//        resp.put("orderId", rec.getOrderId());
//        resp.put("status", rec.getStatus());
//        resp.put("paymentId", rec.getGatewayPaymentId()); // NULL allowed here
//
//        return ResponseEntity.ok(resp);
//    }
//
//
//
//
//}

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

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) throws Exception {

        PaymentResponse resp = service.createPaymentOrder(request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getStatus(@PathVariable("orderId") String orderId) {
        return service.findByOrderId(orderId)
                .map(rec -> ResponseEntity.ok(
                        java.util.Map.of(
                                "orderId", rec.getOrderId(),
                                "status", rec.getStatus(),
                                "paymentId", rec.getGatewayPaymentId()
                        )
                ))
                .orElseGet(() ->
                        ResponseEntity.status(404).body(
                                java.util.Map.of("error", "Order not found")
                        )
                );
    }
}
