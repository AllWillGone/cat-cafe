package com.catcafe.app.ui;

import com.catcafe.app.model.ProductDetail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ProductRules {
    private ProductRules() {
    }

    public static boolean isCatCafeTicket(ProductDetail product) {
        if (product == null || product.productName == null) {
            return false;
        }
        String name = product.productName.toLowerCase(Locale.ROOT);
        return name.contains("撸猫券")
                || name.contains("撸猫卷")
                || name.contains("cat cafe ticket")
                || name.contains("cat ticket");
    }

    public static List<ProductDetail> catCafeTickets(List<ProductDetail> products) {
        List<ProductDetail> result = new ArrayList<>();
        if (products == null) {
            return result;
        }
        for (ProductDetail product : products) {
            if (isCatCafeTicket(product)) {
                result.add(product);
            }
        }
        return result;
    }

    public static List<ProductDetail> productsWithoutTickets(List<ProductDetail> products) {
        List<ProductDetail> result = new ArrayList<>();
        if (products == null) {
            return result;
        }
        for (ProductDetail product : products) {
            if (!isCatCafeTicket(product)) {
                result.add(product);
            }
        }
        return result;
    }

    public static List<ProductDetail> recommendedProducts(List<ProductDetail> serviceSource,
                                                          List<ProductDetail> productSource,
                                                          int limit) {
        List<ProductDetail> result = new ArrayList<>();
        Set<Long> added = new HashSet<>();
        append(result, added, catCafeTickets(serviceSource), 1);
        append(result, added, productsWithoutTickets(productSource), limit);
        return result;
    }

    private static void append(List<ProductDetail> result, Set<Long> added, List<ProductDetail> source, int limit) {
        if (source == null) {
            return;
        }
        for (ProductDetail product : source) {
            if (product == null || added.contains(product.productId)) {
                continue;
            }
            result.add(product);
            added.add(product.productId);
            if (result.size() >= limit) {
                return;
            }
        }
    }
}
