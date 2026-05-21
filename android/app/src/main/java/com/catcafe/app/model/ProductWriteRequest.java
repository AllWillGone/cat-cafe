package com.catcafe.app.model;

import java.math.BigDecimal;

public class ProductWriteRequest {
    public String productName;
    public Integer category;
    public BigDecimal price;
    public Integer stockQuantity;
    public String imageUrl;
    public String description;
    public Integer status;

    public ProductWriteRequest(String productName, Integer category, BigDecimal price, Integer stockQuantity,
                               String imageUrl, String description, Integer status) {
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.description = description;
        this.status = status;
    }
}
