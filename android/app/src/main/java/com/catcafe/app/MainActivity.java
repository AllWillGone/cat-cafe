package com.catcafe.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.UserDetail;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.ui.ActivityFragment;
import com.catcafe.app.ui.AdminModuleFragment;
import com.catcafe.app.ui.HomeFragment;
import com.catcafe.app.ui.MineFragment;
import com.catcafe.app.ui.ServiceFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private BottomNavigationView bottomNav;
    private MaterialButton adminLogoutButton;
    private Boolean adminNavigationMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);
        applyStatusBarInset();

        bottomNav = findViewById(R.id.bottomNav);
        adminLogoutButton = findViewById(R.id.adminLogoutButton);
        adminLogoutButton.setOnClickListener(v -> exitAdminMode());
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (sessionManager.isAdmin()) {
                return showAdminModule(id);
            }
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
        configureNavigationForRole();

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(sessionManager.isAdmin() ? R.id.nav_admin_comments : R.id.nav_home);
        }
        verifyTokenIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        configureNavigationForRole();
    }

    public void openMinePage() {
        bottomNav.setSelectedItemId(R.id.nav_mine);
    }

    public void openCartPage() {
        startActivity(new Intent(this, com.catcafe.app.ui.CartActivity.class));
    }

    public void openOrdersPage() {
        startActivity(new Intent(this, com.catcafe.app.ui.OrderListActivity.class));
    }

    public void refreshNavigationForRole() {
        configureNavigationForRole();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void configureNavigationForRole() {
        if (bottomNav == null) {
            return;
        }
        boolean adminMode = sessionManager.isAdmin();
        if (adminNavigationMode != null && adminNavigationMode == adminMode) {
            return;
        }

        int selectedId = bottomNav.getSelectedItemId();
        Menu menu = bottomNav.getMenu();
        menu.clear();
        if (adminMode) {
            bottomNav.inflateMenu(R.menu.bottom_nav_admin_menu);
        } else {
            bottomNav.inflateMenu(R.menu.bottom_nav_menu);
        }
        adminLogoutButton.setVisibility(adminMode ? View.VISIBLE : View.GONE);

        adminNavigationMode = adminMode;
        if (selectedId == 0 || menu.findItem(selectedId) == null) {
            selectedId = adminMode ? R.id.nav_admin_comments : R.id.nav_home;
        }
        bottomNav.setSelectedItemId(selectedId);
        if (adminMode && selectedId == R.id.nav_admin_comments) {
            showAdminModule(selectedId);
        }
    }

    private boolean showAdminModule(int id) {
        if (id == R.id.nav_admin_comments) {
            showFragment(AdminModuleFragment.newInstance(AdminModuleFragment.MODULE_COMMENTS));
            return true;
        } else if (id == R.id.nav_admin_orders) {
            showFragment(AdminModuleFragment.newInstance(AdminModuleFragment.MODULE_ORDERS));
            return true;
        } else if (id == R.id.nav_admin_products) {
            showFragment(AdminModuleFragment.newInstance(AdminModuleFragment.MODULE_PRODUCTS));
            return true;
        } else if (id == R.id.nav_admin_cats) {
            showFragment(AdminModuleFragment.newInstance(AdminModuleFragment.MODULE_CATS));
            return true;
        } else if (id == R.id.nav_admin_users) {
            showFragment(AdminModuleFragment.newInstance(AdminModuleFragment.MODULE_USERS));
            return true;
        }
        return false;
    }

    private void exitAdminMode() {
        sessionManager.clear();
        adminNavigationMode = null;
        configureNavigationForRole();
        showFragment(new HomeFragment());
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void applyStatusBarInset() {
        View root = findViewById(R.id.mainRoot);
        View fragmentContainer = findViewById(R.id.fragmentContainer);
        int initialLeft = fragmentContainer.getPaddingLeft();
        int initialTop = fragmentContainer.getPaddingTop();
        int initialRight = fragmentContainer.getPaddingRight();
        int initialBottom = fragmentContainer.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            fragmentContainer.setPadding(
                    initialLeft,
                    initialTop + statusBars.top,
                    initialRight,
                    initialBottom
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
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
