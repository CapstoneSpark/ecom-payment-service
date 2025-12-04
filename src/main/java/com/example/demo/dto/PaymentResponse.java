
package com.example.demo.dto;

public class PaymentResponse {
    private String orderId; // razorpay order id
    private Long amount;
    private String currency;
    private String key;

    // getters & setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
