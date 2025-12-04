

package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PaymentRequest {
    @NotNull
    private Long amount; // paise
    private String currency = "INR";
    private Long userId;
    private List<OrderItemInternalDto> items; // raw items from frontend (paise)
    private ShippingDto shipping;

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<OrderItemInternalDto> getItems() { return items; }
    public void setItems(List<OrderItemInternalDto> items) { this.items = items; }

    public ShippingDto getShipping() { return shipping; }
    public void setShipping(ShippingDto shipping) { this.shipping = shipping; }
}
