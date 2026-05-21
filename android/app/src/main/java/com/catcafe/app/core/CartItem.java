package com.catcafe.app.core;

import com.catcafe.app.model.ProductDetail;

public class CartItem {
    public final ProductDetail product;
    public int quantity;

    public CartItem(ProductDetail product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}
