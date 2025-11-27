//package com.example.demo.entity;
//
//import jakarta.persistence.*;
//import java.math.BigDecimal;
//import java.time.Instant;
//
//@Entity
//@Table(name = "payments")
//public class PaymentRecord {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String orderId; // your system's order id
//
//    @Column(nullable = false)
//    private String razorpayOrderId; // Razorpay order id
//
//    @Column(nullable = false)
//    private String userId;
//
//    @Column(nullable = false, precision = 18, scale = 2)
//    private BigDecimal amount;
//
//    @Column(nullable = false)
//    private String currency;
//
//    /**
//     * INITIATED, AWAITING_PAYMENT, COMPLETED, FAILED, REFUNDED
//     */
//    @Column(nullable = false)
//    private String status;
//
//    private String gatewayPaymentId; // actual payment id from Razorpay after capture
//
//    @Lob
//    private String gatewayPayload; // raw JSON from webhook or gateway response
//
//    private Instant createdAt;
//    private Instant updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        createdAt = Instant.now();
//        updatedAt = createdAt;
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        updatedAt = Instant.now();
//    }
//
//    // getters and setters (omitted for brevity) — add them below
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getOrderId() { return orderId; }
//    public void setOrderId(String orderId) { this.orderId = orderId; }
//
//    public String getRazorpayOrderId() { return razorpayOrderId; }
//    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
//
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//
//    public BigDecimal getAmount() { return amount; }
//    public void setAmount(BigDecimal amount) { this.amount = amount; }
//
//    public String getCurrency() { return currency; }
//    public void setCurrency(String currency) { this.currency = currency; }
//
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//
//    public String getGatewayPaymentId() { return gatewayPaymentId; }
//    public void setGatewayPaymentId(String gatewayPaymentId) { this.gatewayPaymentId = gatewayPaymentId; }
//
//    public String getGatewayPayload() { return gatewayPayload; }
//    public void setGatewayPayload(String gatewayPayload) { this.gatewayPayload = gatewayPayload; }
//
//    public Instant getCreatedAt() { return createdAt; }
//    public Instant getUpdatedAt() { return updatedAt; }
//}
package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payment_records")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true)
    private String orderId; // Razorpay order id

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "status")
    private String status;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Lob
    @Column(name = "items_json", columnDefinition = "LONGTEXT")
    private String itemsJson;

    @Lob
    @Column(name = "shipping_json", columnDefinition = "LONGTEXT")
    private String shippingJson;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }

    // -------------------------
    // Getters and Setters
    // -------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public void setGatewayPaymentId(String gatewayPaymentId) {
        this.gatewayPaymentId = gatewayPaymentId;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }

    public String getShippingJson() {
        return shippingJson;
    }

    public void setShippingJson(String shippingJson) {
        this.shippingJson = shippingJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
