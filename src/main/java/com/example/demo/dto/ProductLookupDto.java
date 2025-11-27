package com.example.demo.dto;

public class ProductLookupDto {

    private Long productId; // <-- match entity JSON field
    private String sku;
    private String name;
    private Double price; // <-- product.price is Double in entity

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
