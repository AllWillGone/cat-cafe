package com.catcafe.app.model;

import java.util.List;

public class OrderCreateRequest {
    public List<OrderItemRequest> items;
    public Integer paymentMethod;
    public String userPhone;
    public String userName;
    public String orderNote;

    public OrderCreateRequest(List<OrderItemRequest> items, Integer paymentMethod, String userPhone, String userName, String orderNote) {
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.userPhone = userPhone;
        this.userName = userName;
        this.orderNote = orderNote;
    }
}
