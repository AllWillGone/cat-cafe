package com.catcafe.app.model;

public class OrderUpdateRequest {
    public Integer orderStatus;
    public String userPhone;
    public String userName;
    public String orderNote;

    public OrderUpdateRequest(Integer orderStatus, String userPhone, String userName, String orderNote) {
        this.orderStatus = orderStatus;
        this.userPhone = userPhone;
        this.userName = userName;
        this.orderNote = orderNote;
    }
}
