package com.catcafe.app.ui;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public abstract class AdminListActivityBase extends BaseToolbarActivity {
    protected SwipeRefreshLayout refreshLayout;
    protected RecyclerView recyclerView;
    protected Spinner filterSpinner;
    protected TextInputEditText keywordInput;
    protected TextInputLayout keywordLayout;
    protected MaterialButton addButton;
    protected MaterialButton prevButton;
    protected MaterialButton nextButton;
    protected TextView pageText;
    protected int page = 0;
    protected int total = 0;
    protected SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);
        setupToolbar(R.id.adminToolbar);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "请先使用管理员账号登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refreshLayout = findViewById(R.id.adminRefresh);
        recyclerView = findViewById(R.id.adminRecycler);
        filterSpinner = findViewById(R.id.adminFilterSpinner);
        keywordInput = findViewById(R.id.adminKeywordInput);
        keywordLayout = findViewById(R.id.adminKeywordLayout);
        addButton = findViewById(R.id.adminAddButton);
        prevButton = findViewById(R.id.adminPrevButton);
        nextButton = findViewById(R.id.adminNextButton);
        pageText = findViewById(R.id.adminPageText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.adminSearchButton).setOnClickListener(v -> {
            page = 0;
            loadData();
        });
        keywordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                page = 0;
                loadData();
                return true;
            }
            return false;
        });
        refreshLayout.setOnRefreshListener(this::loadData);
        prevButton.setOnClickListener(v -> {
            if (page > 0) {
                page--;
                loadData();
            }
        });
        nextButton.setOnClickListener(v -> {
            if ((page + 1) * AppConfig.PAGE_LIMIT < total) {
                page++;
                loadData();
            }
        });

        configure();
        loadData();
    }

    protected abstract void configure();

    protected abstract void loadData();

    protected void setTitleText(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    protected void setFilterLabels(String[] labels) {
        filterSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
    }

    protected String keyword() {
        String text = keywordInput.getText() == null ? "" : keywordInput.getText().toString().trim();
        return text.isEmpty() ? null : text;
    }

    protected int skip() {
        return page * AppConfig.PAGE_LIMIT;
    }

    protected void updatePaging(int total) {
        this.total = total;
        int from = total == 0 ? 0 : skip() + 1;
        int to = Math.min(skip() + AppConfig.PAGE_LIMIT, total);
        pageText.setText(from + "-" + to + " / " + total);
        prevButton.setEnabled(page > 0);
        nextButton.setEnabled((page + 1) * AppConfig.PAGE_LIMIT < total);
    }

    protected void showLoading(boolean loading) {
        refreshLayout.setRefreshing(loading);
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
