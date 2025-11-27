//package com.example.demo.dto;
//
//import java.util.List;
//
//public class OrderRequestDto {
//    private Long userId;
//    private String idempotencyKey;
//    private String paymentMethod;
//    private ShippingDto shipping;
//    private List<OrderItemDto> items;
//
//    // getters & setters
//    public Long getUserId() { return userId; }
//    public void setUserId(Long userId) { this.userId = userId; }
//    public String getIdempotencyKey() { return idempotencyKey; }
//    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
//    public String getPaymentMethod() { return paymentMethod; }
//    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
//    public ShippingDto getShipping() { return shipping; }
//    public void setShipping(ShippingDto shipping) { this.shipping = shipping; }
//    public java.util.List<OrderItemDto> getItems() { return items; }
//    public void setItems(java.util.List<OrderItemDto> items) { this.items = items; }
//}


package com.example.demo.dto;

import java.util.List;

public class OrderRequestDto {
    private Long userId;
    private String idempotencyKey;
    private String paymentMethod;
    private ShippingDto shipping;
    private List<OrderItemDto> items;
    private Long cartId; // optional - included in OrderService usage

    // getters & setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public ShippingDto getShipping() { return shipping; }
    public void setShipping(ShippingDto shipping) { this.shipping = shipping; }

    public java.util.List<OrderItemDto> getItems() { return items; }
    public void setItems(java.util.List<OrderItemDto> items) { this.items = items; }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
}
