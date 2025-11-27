//package com.example.demo.dto;
//
//import jakarta.validation.constraints.DecimalMin;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import java.math.BigDecimal;
//
//public class PaymentRequest {
//
//    @NotBlank
//    private String orderId;
//
//    @NotBlank
//    private String userId;
//
//    @NotNull
//    @DecimalMin("1.0")
//    private BigDecimal amount;
//
//    @NotBlank
//    private String currency; // INR
//
//    // getters/setters
//
//    public String getOrderId() { return orderId; }
//    public void setOrderId(String orderId) { this.orderId = orderId; }
//
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//
//    public BigDecimal getAmount() { return amount; }
//    public void setAmount(BigDecimal amount) { this.amount = amount; }
//
//    public String getCurrency() { return currency; }
//    public void setCurrency(String currency) { this.currency = currency; }
//}

//
//package com.example.demo.dto;
//
//import jakarta.validation.constraints.NotNull;
//import java.util.List;
//
//public class PaymentRequest {
//    @NotNull
//    private Long amount; // paise
//    private String currency = "INR";
//    private Long userId;
//    private List<OrderItemDto> items;
//    private ShippingDto shipping;
//
//    // getters & setters
//    // ... (generate in IDE)
//    public Long getAmount() { return amount; }
//    public void setAmount(Long amount) { this.amount = amount; }
//    public String getCurrency() { return currency; }
//    public void setCurrency(String currency) { this.currency = currency; }
//    public Long getUserId() { return userId; }
//    public void setUserId(Long userId) { this.userId = userId; }
//    public List<OrderItemDto> getItems() { return items; }
//    public void setItems(List<OrderItemDto> items) { this.items = items; }
//    public ShippingDto getShipping() { return shipping; }
//    public void setShipping(ShippingDto shipping) { this.shipping = shipping; }
//}


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
