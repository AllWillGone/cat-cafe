package com.catcafe.app.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.catcafe.app.R;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.LoginRequest;
import com.catcafe.app.model.LoginResponse;
import com.catcafe.app.model.RegisterRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AuthActivity extends BaseToolbarActivity {
    private static final String TAG = "CatCafeAuth";
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_REGISTER = "register";

    private boolean registerMode;
    private TextInputEditText accountInput;
    private TextInputEditText phoneInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private TextInputEditText userNameInput;
    private MaterialButton submitButton;
    private MaterialButton switchButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        setupToolbar(R.id.authToolbar);
        sessionManager = new SessionManager(this);
        registerMode = MODE_REGISTER.equals(getIntent().getStringExtra(EXTRA_MODE));

        accountInput = findViewById(R.id.authAccountInput);
        phoneInput = findViewById(R.id.authPhoneInput);
        passwordInput = findViewById(R.id.authPasswordInput);
        confirmInput = findViewById(R.id.authConfirmInput);
        userNameInput = findViewById(R.id.authUserNameInput);
        submitButton = findViewById(R.id.authSubmitButton);
        switchButton = findViewById(R.id.authSwitchButton);

        submitButton.setOnClickListener(v -> submit());
        switchButton.setOnClickListener(v -> {
            registerMode = !registerMode;
            renderMode();
        });
        renderMode();
    }

    private void renderMode() {
        int visible = View.VISIBLE;
        int gone = View.GONE;
        if (registerMode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("注册");
            }
            accountInput.setVisibility(gone);
            phoneInput.setVisibility(visible);
            confirmInput.setVisibility(visible);
            userNameInput.setVisibility(visible);
            submitButton.setText("注册");
            switchButton.setText("切换到登录");
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("登录");
            }
            accountInput.setVisibility(visible);
            phoneInput.setVisibility(gone);
            confirmInput.setVisibility(gone);
            userNameInput.setVisibility(gone);
            submitButton.setText("登录");
            switchButton.setText("切换到注册");
        }
    }

    private void submit() {
        String password = textOf(passwordInput);
        if (password.isEmpty()) {
            toast("请输入密码");
            return;
        }

        if (registerMode) {
            String userName = textOf(userNameInput);
            String phone = textOf(phoneInput);
            String confirm = textOf(confirmInput);
            if (userName.isEmpty() || phone.isEmpty()) {
                toast("请填写用户名和手机号");
                return;
            }
            if (!password.equals(confirm)) {
                toast("两次密码不一致");
                return;
            }
            NetworkHelper.enqueue(this,
                    ApiClient.getService(this).register(new RegisterRequest(userName, password, phone)),
                    new ApiCallback<LoginResponse>() {
                        @Override
                        public void onSuccess(LoginResponse data) {
                            Log.d(TAG, "register success userId=" + data.userId);
                            sessionManager.saveLogin(data);
                            Toast.makeText(AuthActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Log.w(TAG, "register error: " + message);
                            toast(message);
                        }
                    });
        } else {
            String account = textOf(accountInput);
            if (account.isEmpty()) {
                toast("请输入用户名或手机号");
                return;
            }
            NetworkHelper.enqueue(this,
                    ApiClient.getService(this).login(LoginRequest.byAccount(account, password)),
                    new ApiCallback<LoginResponse>() {
                        @Override
                        public void onSuccess(LoginResponse data) {
                            Log.d(TAG, "login success userType=" + data.userType);
                            if (data.userType == 1) {
                                toast("管理员账号请从首页管理员入口登录");
                                return;
                            }
                            sessionManager.saveLogin(data);
                            Toast.makeText(AuthActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Log.w(TAG, "login error: " + message);
                            toast(message);
                        }
                    });
        }
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
