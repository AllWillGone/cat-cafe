package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.core.CartItem;
import com.catcafe.app.core.CartManager;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.OrderCreateRequest;
import com.catcafe.app.model.OrderItemRequest;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.model.UserDetail;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseToolbarActivity {
    public static Intent intent(Context context) {
        return new Intent(context, CartActivity.class);
    }

    private CartManager cart;
    private CartAdapter adapter;
    private TextView totalText;
    private SessionManager sessionManager;
    private boolean submitting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupToolbar(R.id.cartToolbar);
        cart = CartManager.getInstance();
        sessionManager = new SessionManager(this);

        RecyclerView list = findViewById(R.id.cartList);
        totalText = findViewById(R.id.cartTotalText);
        TextInputEditText nameInput = findViewById(R.id.cartUserNameInput);
        TextInputEditText phoneInput = findViewById(R.id.cartUserPhoneInput);
        TextInputEditText noteInput = findViewById(R.id.cartNoteInput);
        MaterialButton submitButton = findViewById(R.id.cartSubmitButton);
        MaterialButton clearButton = findViewById(R.id.cartClearButton);

        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this::renderTotal);
        list.setAdapter(adapter);
        adapter.submit(cart.getItems());
        fillUserInfo(nameInput, phoneInput);

        renderTotal();
        clearButton.setOnClickListener(v -> {
            cart.clear();
            adapter.submit(cart.getItems());
            renderTotal();
        });
        submitButton.setOnClickListener(v -> {
            if (submitting) {
                return;
            }
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "请先登录后购买", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AuthActivity.class));
                return;
            }
            List<CartItem> cartItems = cart.getItems();
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "购物车为空", Toast.LENGTH_SHORT).show();
                return;
            }
            List<OrderItemRequest> items = new ArrayList<>();
            for (CartItem item : cartItems) {
                items.add(new OrderItemRequest(item.product.productId, item.quantity));
            }
            String userName = textOf(nameInput);
            String userPhone = textOf(phoneInput);
            if (userName.isEmpty() || userPhone.isEmpty()) {
                Toast.makeText(this, "请填写收货信息", Toast.LENGTH_SHORT).show();
                return;
            }
            submitButton.setEnabled(false);
            submitting = true;
            refreshCartBeforeSubmit(cartItems, 0, () -> createOrder(items, userPhone, userName, textOf(noteInput), submitButton));
        });
    }

    private void refreshCartBeforeSubmit(List<CartItem> cartItems, int index, Runnable onReady) {
        if (index >= cartItems.size()) {
            onReady.run();
            return;
        }
        CartItem item = cartItems.get(index);
        NetworkHelper.enqueue(this, ApiClient.getService(this).getProduct(item.product.productId), new ApiCallback<ProductDetail>() {
            @Override
            public void onSuccess(ProductDetail product) {
                if (product.status == 0) {
                    finishFailedSubmit(product.productName + " 已下架，请从购物车移除后再下单");
                    return;
                }
                if (product.stockQuantity < item.quantity) {
                    cart.setQuantity(product.productId, product.stockQuantity);
                    adapter.submit(cart.getItems());
                    renderTotal();
                    if (product.stockQuantity <= 0) {
                        finishFailedSubmit(product.productName + " 库存不足，已从购物车移除");
                    } else {
                        finishFailedSubmit(product.productName + " 库存仅剩 " + product.stockQuantity + " 件，已调整数量");
                    }
                    return;
                }
                refreshCartBeforeSubmit(cartItems, index + 1, onReady);
            }

            @Override
            public void onError(String message) {
                finishFailedSubmit(message);
            }
        });
    }

    private void createOrder(List<OrderItemRequest> items, String userPhone, String userName, String note, MaterialButton submitButton) {
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).createOrder(new OrderCreateRequest(items, 0, userPhone, userName, note)),
                new ApiCallback<com.catcafe.app.model.BatchOrderResponse>() {
                    @Override
                    public void onSuccess(com.catcafe.app.model.BatchOrderResponse data) {
                        submitting = false;
                        submitButton.setEnabled(true);
                        cart.clear();
                        adapter.submit(cart.getItems());
                        renderTotal();
                        startActivity(OrderListActivity.intent(CartActivity.this));
                        finish();
                        Toast.makeText(CartActivity.this, "下单成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        finishFailedSubmit(message);
                    }
                });
    }

    private void finishFailedSubmit(String message) {
        submitting = false;
        MaterialButton submitButton = findViewById(R.id.cartSubmitButton);
        submitButton.setEnabled(true);
        Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void fillUserInfo(TextInputEditText nameInput, TextInputEditText phoneInput) {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        nameInput.setText(sessionManager.getUserName());
        phoneInput.setText(sessionManager.getUserPhone());
        NetworkHelper.enqueue(this, ApiClient.getService(this).getMe(), new ApiCallback<UserDetail>() {
            @Override
            public void onSuccess(UserDetail data) {
                sessionManager.saveUserDetail(data);
                nameInput.setText(data.userName);
                phoneInput.setText(data.userPhone);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderTotal() {
        totalText.setText("合计 " + UiText.price(cart.getTotalAmount()));
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
