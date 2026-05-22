package com.catcafe.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.UserDetail;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.ui.ActivityFragment;
import com.catcafe.app.ui.HomeFragment;
import com.catcafe.app.ui.MineFragment;
import com.catcafe.app.ui.ServiceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_service) {
                showFragment(new ServiceFragment());
                return true;
            } else if (id == R.id.nav_activity) {
                showFragment(new ActivityFragment());
                return true;
            } else if (id == R.id.nav_mine) {
                showFragment(new MineFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        verifyTokenIfNeeded();
    }

    public void openMinePage() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_mine);
    }

    public void openCartPage() {
        startActivity(new Intent(this, com.catcafe.app.ui.CartActivity.class));
    }

    public void openOrdersPage() {
        startActivity(new Intent(this, com.catcafe.app.ui.OrderListActivity.class));
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void verifyTokenIfNeeded() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).getMe(), new ApiCallback<UserDetail>() {
            @Override
            public void onSuccess(UserDetail data) {
                sessionManager.saveUserDetail(data);
            }

            @Override
            public void onError(String message) {
                sessionManager.clear();
            }
        });
    }
}
