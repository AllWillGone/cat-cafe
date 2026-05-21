package com.catcafe.app.model;

import java.math.BigDecimal;
import java.util.List;

public class BatchOrderResponse {
    public String batchNo;
    public long userId;
    public int orderStatus;
    public Integer paymentMethod;
    public String orderTime;
    public String userPhone;
    public String userName;
    public String orderNote;
    public BigDecimal totalAmount;
    public List<OrderDetailItem> items;
}
