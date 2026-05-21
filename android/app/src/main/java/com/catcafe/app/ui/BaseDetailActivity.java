package com.catcafe.app.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.catcafe.app.core.CartManager;
import com.catcafe.app.core.SessionManager;

public abstract class BaseDetailActivity extends BaseToolbarActivity {
    protected SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
    }

    protected void requireLogin(String message) {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            startActivity(new android.content.Intent(this, AuthActivity.class));
        }
    }

    protected CartManager cart() {
        return CartManager.getInstance();
    }
}
