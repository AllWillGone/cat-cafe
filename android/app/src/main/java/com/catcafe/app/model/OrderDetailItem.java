package com.catcafe.app.model;

import java.math.BigDecimal;

public class OrderDetailItem {
    public long orderId;
    public long productId;
    public String productName;
    public int productQuantity;
    public BigDecimal totalAmount;
}
