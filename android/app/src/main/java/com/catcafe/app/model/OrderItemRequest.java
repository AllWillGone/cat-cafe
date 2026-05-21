package com.catcafe.app.model;

public class OrderItemRequest {
    public long productId;
    public int productQuantity;

    public OrderItemRequest(long productId, int productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }
}
