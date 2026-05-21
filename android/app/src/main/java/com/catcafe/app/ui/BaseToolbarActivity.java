package com.catcafe.app.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.catcafe.app.R;
import com.google.android.material.appbar.MaterialToolbar;

public abstract class BaseToolbarActivity extends AppCompatActivity {
    protected void setupToolbar(int toolbarId) {
        MaterialToolbar toolbar = findViewById(toolbarId);
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

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
