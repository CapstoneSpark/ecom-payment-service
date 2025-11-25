package com.example.demo.service.impl;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.entity.PaymentRecord;
import com.example.demo.events.EventPublisher;
import com.example.demo.gateway.GatewayOrderResponse;
import com.example.demo.gateway.RazorpayGateway;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentProcessorServiceImpl implements PaymentProcessorService {

    private final PaymentRepository repository;
    private final RazorpayGateway gateway;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    public PaymentProcessorServiceImpl(PaymentRepository repository,
                                       RazorpayGateway gateway,
                                       ObjectMapper objectMapper,
                                       EventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public PaymentResponse createPaymentOrder(PaymentRequest req) throws Exception {
        // Basic check: ensure not already created
        Optional<PaymentRecord> existing = repository.findByOrderId(req.getOrderId());
        if (existing.isPresent()) {
            PaymentRecord r = existing.get();
            return new PaymentResponse(r.getStatus(), r.getOrderId(), r.getRazorpayOrderId(), "Already exists");
        }

        // Create Razorpay order
        GatewayOrderResponse gor = gateway.createOrder(req.getOrderId(), req.getAmount(), req.getCurrency(), Map.of());

        // Save in DB
        PaymentRecord rec = new PaymentRecord();
        rec.setOrderId(req.getOrderId());
        rec.setRazorpayOrderId(gor.getRazorpayOrderId());
        rec.setUserId(req.getUserId());
        rec.setAmount(req.getAmount());
        rec.setCurrency(req.getCurrency());
        rec.setStatus("AWAITING_PAYMENT");
        rec.setGatewayPayload(objectMapper.writeValueAsString(gor.getRaw()));
        repository.save(rec);

        PaymentResponse resp = new PaymentResponse("AWAITING_PAYMENT", req.getOrderId(), gor.getRazorpayOrderId(), "Created Razorpay order");
        return resp;
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) throws Exception {
        // Verify signature
        if (!gateway.verifyWebhookSignature(payload, signature)) {
            throw new RuntimeException("Invalid webhook signature");
        }

        Map<String, Object> map = objectMapper.readValue(payload, Map.class);
        String event = (String) map.get("event");
        Map<String, Object> data = (Map<String, Object>) map.get("payload");

        // Razorpay webhook payload structure varies; for payments, find payment entity
        // Support: payment.captured, payment.failed, refund.processed, etc.
        if ("payment.captured".equals(event) || "payment.authorized".equals(event) || "order.paid".equals(event) || event != null && event.contains("payment")) {
            // try to find payment id or order id
            // The nested structure for payment id: payload.payment.entity.id (or payload.payment.entity.order_id)
            Map<String,Object> paymentEntity = null;
            if (data.containsKey("payment")) {
                Map<String,Object> paymentMap = (Map<String,Object>) data.get("payment");
                if (paymentMap.containsKey("entity")) paymentEntity = (Map<String,Object>) paymentMap.get("entity");
            }
            if (paymentEntity != null) {
                String razorpayOrderId = (String) paymentEntity.get("order_id");
                String paymentId = (String) paymentEntity.get("id");
                // update record
                repository.findByRazorpayOrderId(razorpayOrderId).ifPresent(rec -> {
                    rec.setStatus("COMPLETED");
                    rec.setGatewayPaymentId(paymentId);
                    try {
                        rec.setGatewayPayload(objectMapper.writeValueAsString(map));
                    } catch (Exception e) { /* ignore */ }
                    repository.save(rec);
                    eventPublisher.publish("PaymentCompleted", Map.of(
                            "orderId", rec.getOrderId(),
                            "paymentId", rec.getId(),
                            "razorpayPaymentId", paymentId
                    ));
                });
            }
        } else if (event != null && event.contains("refund")) {
            // handle refund events
            // find by payment id or order id
            Map<String,Object> refundEntity = null;
            if (data.containsKey("refund")) {
                Map<String,Object> refundMap = (Map<String,Object>) data.get("refund");
                if (refundMap.containsKey("entity")) refundEntity = (Map<String,Object>) refundMap.get("entity");
            }
            if (refundEntity != null) {
                String paymentId = (String) refundEntity.get("payment_id");
                // find record by gatewayPaymentId
                repository.findAll().stream()
                        .filter(r -> paymentId.equals(r.getGatewayPaymentId()))
                        .findFirst()
                        .ifPresent(rec -> {
                            rec.setStatus("REFUNDED");
                            try { rec.setGatewayPayload(objectMapper.writeValueAsString(map)); } catch (Exception e) {}
                            repository.save(rec);
                            eventPublisher.publish("RefundProcessed", Map.of(
                                    "orderId", rec.getOrderId(),
                                    "paymentId", rec.getId(),
                                    "razorpayPaymentId", paymentId
                            ));
                        });
            }
        } else {
            // other events: you can log or store
            eventPublisher.publish("PaymentWebhookEvent", Map.of("event", event));
        }
    }
}
