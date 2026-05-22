package com.catcafe.app.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.catcafe.app.R;
import com.google.android.material.appbar.MaterialToolbar;

public abstract class BaseToolbarActivity extends AppCompatActivity {
    protected void setupToolbar(int toolbarId) {
        MaterialToolbar toolbar = findViewById(toolbarId);
        applyStatusBarInset(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.cat_text));
        toolbar.setNavigationContentDescription("返回");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void applyStatusBarInset(MaterialToolbar toolbar) {
        int initialLeft = toolbar.getPaddingLeft();
        int initialTop = toolbar.getPaddingTop();
        int initialRight = toolbar.getPaddingRight();
        int initialBottom = toolbar.getPaddingBottom();
        int initialMinHeight = toolbar.getMinimumHeight();
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            toolbar.setPadding(
                    initialLeft,
                    initialTop + statusBars.top,
                    initialRight,
                    initialBottom
            );
            toolbar.setMinimumHeight(initialMinHeight + statusBars.top);
            return insets;
        });
        ViewCompat.requestApplyInsets(toolbar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
