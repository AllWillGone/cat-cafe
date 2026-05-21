package com.catcafe.app.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.catcafe.app.R;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.LoginRequest;
import com.catcafe.app.model.LoginResponse;
import com.catcafe.app.model.RegisterRequest;
import com.catcafe.app.model.ResetPasswordByPhoneRequest;
import com.catcafe.app.model.SendSmsCodeRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

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
    private MaterialButton forgotPasswordButton;
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
        forgotPasswordButton = findViewById(R.id.authForgotPasswordButton);

        submitButton.setOnClickListener(v -> submit());
        switchButton.setOnClickListener(v -> {
            registerMode = !registerMode;
            renderMode();
        });
        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());
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
            forgotPasswordButton.setVisibility(gone);
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
            forgotPasswordButton.setVisibility(visible);
            switchButton.setText("切换到注册");
        }
    }

    private void showForgotPasswordDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        content.setPadding(padding, 0, padding, 0);

        TextInputEditText phoneInput = newDialogInput("手机号", InputType.TYPE_CLASS_PHONE);
        TextInputEditText codeInput = newDialogInput("验证码", InputType.TYPE_CLASS_NUMBER);
        TextInputEditText newPasswordInput = newDialogInput(
                "新密码",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        content.addView(wrapInput(phoneInput, "手机号", 0));
        content.addView(wrapInput(codeInput, "验证码", 12));
        content.addView(wrapInput(newPasswordInput, "新密码", 12));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("忘记密码")
                .setView(content)
                .setNegativeButton("取消", null)
                .setNeutralButton("获取验证码", null)
                .setPositiveButton("重置密码", null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)
                    .setOnClickListener(v -> sendSmsCode(phoneInput));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> resetPasswordByPhone(dialog, phoneInput, codeInput, newPasswordInput));
        });
        dialog.show();
    }

    private TextInputEditText newDialogInput(String hint, int inputType) {
        TextInputEditText input = new TextInputEditText(this);
        input.setHint(hint);
        input.setInputType(inputType);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        return input;
    }

    private TextInputLayout wrapInput(TextInputEditText input, String hint, int topMarginDp) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(hint);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(topMarginDp);
        layout.setLayoutParams(params);
        layout.addView(input);
        return layout;
    }

    private void sendSmsCode(TextInputEditText phoneInput) {
        String phone = textOf(phoneInput);
        if (phone.length() < 11) {
            toast("请输入正确手机号");
            return;
        }
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).sendSmsCode(new SendSmsCodeRequest(phone)),
                new ApiCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        Object code = data.get("code");
                        if (code != null) {
                            toast("验证码已发送：" + code);
                        } else {
                            toast("验证码已发送");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }

    private void resetPasswordByPhone(androidx.appcompat.app.AlertDialog dialog,
                                      TextInputEditText phoneInput,
                                      TextInputEditText codeInput,
                                      TextInputEditText newPasswordInput) {
        String phone = textOf(phoneInput);
        String code = textOf(codeInput);
        String newPassword = textOf(newPasswordInput);
        if (phone.length() < 11) {
            toast("请输入正确手机号");
            return;
        }
        if (code.length() != 6) {
            toast("请输入 6 位验证码");
            return;
        }
        if (newPassword.length() < 6) {
            toast("新密码至少 6 位");
            return;
        }
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).resetPasswordByPhone(
                        new ResetPasswordByPhoneRequest(phone, code, newPassword)
                ),
                new ApiCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> data) {
                        toast("密码重置成功，请重新登录");
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
