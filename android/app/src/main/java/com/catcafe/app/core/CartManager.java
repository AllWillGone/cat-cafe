package com.catcafe.app.core;

import com.catcafe.app.model.ProductDetail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CartManager {
    private static CartManager instance;

    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    private CartManager() {
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public synchronized void add(ProductDetail product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }
        CartItem item = items.get(product.productId);
        int next = quantity;
        if (item != null) {
            next = item.quantity + quantity;
        }
        if (product.stockQuantity > 0) {
            next = Math.min(next, product.stockQuantity);
        }
        if (item == null) {
            items.put(product.productId, new CartItem(product, next));
        } else {
            item.quantity = next;
        }
    }

    public synchronized void setQuantity(long productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            return;
        }
        if (quantity <= 0) {
            items.remove(productId);
            return;
        }
        int next = quantity;
        if (item.product.stockQuantity > 0) {
            next = Math.min(next, item.product.stockQuantity);
        }
        item.quantity = next;
    }

    public synchronized void remove(long productId) {
        items.remove(productId);
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public synchronized int getCount() {
        int total = 0;
        for (CartItem item : items.values()) {
            total += item.quantity;
        }
        return total;
    }

    public synchronized int getQuantity(long productId) {
        CartItem item = items.get(productId);
        return item == null ? 0 : item.quantity;
    }

    public synchronized BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items.values()) {
            if (item.product.price != null) {
                total = total.add(item.product.price.multiply(BigDecimal.valueOf(item.quantity)));
            }
        }
        return total;
    }
}
