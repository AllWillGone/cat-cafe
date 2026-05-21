package com.catcafe.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.catcafe.app.MainActivity;
import com.catcafe.app.R;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.LoginRequest;
import com.catcafe.app.model.LoginResponse;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminAuthActivity extends BaseToolbarActivity {
    private TextInputEditText accountInput;
    private TextInputEditText passwordInput;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_auth);
        setupToolbar(R.id.adminAuthToolbar);
        sessionManager = new SessionManager(this);
        accountInput = findViewById(R.id.adminAuthAccountInput);
        passwordInput = findViewById(R.id.adminAuthPasswordInput);
        MaterialButton submitButton = findViewById(R.id.adminAuthSubmitButton);
        submitButton.setOnClickListener(v -> submit());
    }

    private void submit() {
        String account = textOf(accountInput);
        String password = textOf(passwordInput);
        if (account.isEmpty() || password.isEmpty()) {
            toast("请输入管理员账号和密码");
            return;
        }
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).login(LoginRequest.byAccount(account, password)),
                new ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse data) {
                        if (data.userType != 1) {
                            toast("当前账号不是管理员");
                            return;
                        }
                        sessionManager.saveLogin(data);
                        Toast.makeText(AdminAuthActivity.this, "管理员登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AdminAuthActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
