package com.catcafe.app.util;

import com.catcafe.app.core.CartItem;
import com.catcafe.app.model.BatchOrderResponse;
import com.catcafe.app.model.LikeDetail;
import com.catcafe.app.model.OrderDetailItem;
import com.catcafe.app.model.ProductDetail;

import java.math.BigDecimal;

public final class UiText {
    private UiText() {
    }

    public static String catStatus(int status) {
        return status == 1 ? "在岗" : "休息";
    }

    public static String productCategory(int category) {
        switch (category) {
            case 0:
                return "服务";
            case 1:
                return "餐饮";
            case 2:
                return "猫咖用品";
            default:
                return "未知分类";
        }
    }

    public static String productStatus(int status) {
        return status == 1 ? "在售" : "已下架";
    }

    public static String productPrice(ProductDetail product) {
        return price(product == null ? null : product.price);
    }

    public static String price(BigDecimal price) {
        if (price == null) {
            return "￥0.00";
        }
        return "￥" + price.toPlainString();
    }

    public static String safeJoin(String first, String second, String separator) {
        String left = first == null ? "" : first.trim();
        String right = second == null ? "" : second.trim();
        if (left.isEmpty()) {
            return right;
        }
        if (right.isEmpty()) {
            return left;
        }
        return left + separator + right;
    }

    public static String orderStatus(int status) {
        switch (status) {
            case 0:
                return "未支付";
            case 1:
                return "已支付";
            case 2:
                return "待取货";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }

    public static String paymentMethod(Integer method) {
        if (method == null) {
            return "未选择";
        }
        switch (method) {
            case 0:
                return "微信";
            case 1:
                return "支付宝";
            case 2:
                return "现金";
            default:
                return "未知";
        }
    }

    public static String batchLabel(BatchOrderResponse order) {
        if (order == null || order.batchNo == null || order.batchNo.trim().isEmpty()) {
            return "订单";
        }
        return "批次 " + order.batchNo;
    }

    public static String orderItemSummary(OrderDetailItem item) {
        return item == null ? "" : item.productName + " x" + item.productQuantity;
    }

    public static String likeType(int type) {
        switch (type) {
            case 0:
                return "商品";
            case 1:
                return "评论";
            case 2:
                return "猫咪";
            default:
                return "未知";
        }
    }

    public static String likeLabel(LikeDetail like) {
        return like == null ? "" : (like.objectName == null ? "点赞" : like.objectName);
    }

    public static String cartSummary(CartItem item) {
        return item == null ? "" : item.product.productName + " x" + item.quantity;
    }
}
