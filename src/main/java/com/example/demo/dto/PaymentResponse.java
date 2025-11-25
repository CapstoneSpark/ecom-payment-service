package com.example.demo.dto;

public class PaymentResponse {
    private String status;
    private String orderId;
    private String razorpayOrderId;
    private String message;
    private String paymentUrl; // if using payment link pattern; optional

    // constructors, getters, setters
    public PaymentResponse() {}
    public PaymentResponse(String status, String orderId, String razorpayOrderId, String message) {
        this.status = status;
        this.orderId = orderId;
        this.razorpayOrderId = razorpayOrderId;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}
