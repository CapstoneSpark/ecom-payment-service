//package com.example.demo.controller;
//
//import com.example.demo.service.PaymentProcessorService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/payments")
//public class WebhookController {
//
//    private final PaymentProcessorService service;
//    private final Logger log = LoggerFactory.getLogger(getClass());
//
//    public WebhookController(PaymentProcessorService service) {
//        this.service = service;
//    }
//
//    /**
//     * Razorpay will POST to this endpoint with X-Razorpay-Signature header.
//     * Make sure to configure the exact webhook URL in Razorpay dashboard and set webhookSecret in application.properties.
//     */
//    @PostMapping("/webhook")
//    public ResponseEntity<String> webhook(@RequestHeader(name = "X-Razorpay-Signature", required = false) String signature,
//                                          @RequestBody String payload) {
//        try {
//            log.info("Received webhook signature={} payload={}", signature, payload);
//            service.handleWebhook(payload, signature);
//            return ResponseEntity.ok("OK");
//        } catch (Exception ex) {
//            log.error("Webhook handling error", ex);
//            return ResponseEntity.status(400).body("error");
//        }
//    }
//}

package com.example.demo.controller;

import com.example.demo.service.PaymentProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class WebhookController {

    private final PaymentProcessorService service;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public WebhookController(PaymentProcessorService service) {
        this.service = service;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String payload) {
        try {
            log.info("Webhook received signature={} payload={}", signature, payload);

            service.handleWebhook(payload, signature);

            return ResponseEntity.ok("OK");

        } catch (Exception ex) {
            log.error("Webhook handling failed", ex);
            return ResponseEntity.status(400).body("error");
        }
    }
}
