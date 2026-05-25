package com.catcafe.app.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.PasswordTransformationMethod;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.widget.Button;
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
import java.util.regex.Pattern;

public class AuthActivity extends BaseToolbarActivity {
    private static final String TAG = "CatCafeAuth";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_REGISTER = "register";

    private boolean registerMode;
    private TextInputLayout accountLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmLayout;
    private TextInputLayout userNameLayout;
    private TextInputEditText accountInput;
    private TextInputEditText phoneInput;
    private TextInputEditText smsCodeInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private TextInputEditText userNameInput;
    private MaterialButton submitButton;
    private MaterialButton switchButton;
    private MaterialButton forgotPasswordButton;
    private MaterialButton sendSmsButton;
    private View smsCodeRow;
    private SessionManager sessionManager;
    private CountDownTimer smsCountdownTimer;
    private String correctSmsCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        setupToolbar(R.id.authToolbar);
        sessionManager = new SessionManager(this);
        registerMode = MODE_REGISTER.equals(getIntent().getStringExtra(EXTRA_MODE));

        accountLayout = findViewById(R.id.authAccountLayout);
        phoneLayout = findViewById(R.id.authPhoneLayout);
        passwordLayout = findViewById(R.id.authPasswordLayout);
        confirmLayout = findViewById(R.id.authConfirmLayout);
        userNameLayout = findViewById(R.id.authUserNameLayout);
        accountInput = findViewById(R.id.authAccountInput);
        phoneInput = findViewById(R.id.authPhoneInput);
        smsCodeInput = findViewById(R.id.authSmsCodeInput);
        passwordInput = findViewById(R.id.authPasswordInput);
        confirmInput = findViewById(R.id.authConfirmInput);
        userNameInput = findViewById(R.id.authUserNameInput);
        submitButton = findViewById(R.id.authSubmitButton);
        switchButton = findViewById(R.id.authSwitchButton);
        forgotPasswordButton = findViewById(R.id.authForgotPasswordButton);
        sendSmsButton = findViewById(R.id.authSendSmsButton);
        smsCodeRow = findViewById(R.id.authSmsCodeRow);

        sendSmsButton.setOnClickListener(v -> sendRegisterSmsCode());
        submitButton.setOnClickListener(v -> submit());
        switchButton.setOnClickListener(v -> {
            registerMode = !registerMode;
            renderMode();
        });
        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());
        setupPasswordToggle(passwordLayout, passwordInput);
        setupPasswordToggle(confirmLayout, confirmInput);
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
            smsCodeRow.setVisibility(visible);
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
            smsCodeRow.setVisibility(gone);
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
        codeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        TextInputEditText newPasswordInput = newDialogInput(
                "新密码",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        TextInputEditText confirmPasswordInput = newDialogInput(
                "确认密码",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        content.addView(wrapInput(phoneInput, "手机号", 0));
        content.addView(wrapInput(codeInput, "验证码", 12));
        content.addView(wrapPasswordInput(newPasswordInput, "新密码", 12));
        content.addView(wrapPasswordInput(confirmPasswordInput, "确认密码", 12));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("手机号找回密码")
                .setView(content)
                .setNegativeButton("取消", null)
                .setNeutralButton("获取验证码", null)
                .setPositiveButton("重置密码", null)
                .create();
        dialog.setOnShowListener(d -> {
            Button smsButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL);
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)
                    .setOnClickListener(v -> sendSmsCode(phoneInput, smsButton));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> resetPasswordByPhone(dialog, phoneInput, codeInput, newPasswordInput, confirmPasswordInput));
        });
        dialog.setOnDismissListener(d -> cancelSmsCountdown());
        dialog.show();
    }

    private TextInputEditText newDialogInput(String hint, int inputType) {
        TextInputEditText input = new TextInputEditText(this);
        input.setHint(hint);
        input.setInputType(inputType);
        input.setTextColor(getColor(R.color.cat_text));
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

    private TextInputLayout wrapPasswordInput(TextInputEditText input, String hint, int topMarginDp) {
        TextInputLayout layout = wrapInput(input, hint, topMarginDp);
        layout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        layout.setEndIconDrawable(R.drawable.ic_visibility_off_24);
        layout.setEndIconContentDescription("显示或隐藏密码");
        setupPasswordToggle(layout, input);
        return layout;
    }

    private void setupPasswordToggle(TextInputLayout layout, TextInputEditText input) {
        setPasswordVisible(layout, input, false);
        layout.setEndIconOnClickListener(v -> {
            boolean willShow = input.getTransformationMethod() instanceof PasswordTransformationMethod;
            setPasswordVisible(layout, input, willShow);
        });
    }

    private void setPasswordVisible(TextInputLayout layout, TextInputEditText input, boolean visible) {
        int selection = Math.max(input.getSelectionStart(), 0);
        input.setTransformationMethod(visible ? null : PasswordTransformationMethod.getInstance());
        layout.setEndIconDrawable(visible ? R.drawable.ic_visibility_24 : R.drawable.ic_visibility_off_24);
        if (input.getText() != null) {
            input.setSelection(Math.min(selection, input.getText().length()));
        }
    }

    private void sendRegisterSmsCode() {
        String phone = textOf(phoneInput);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
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
                            correctSmsCode = String.valueOf(code);
                            toast("验证码已发送（演示：" + correctSmsCode + "）");
                        } else {
                            toast("验证码已发送");
                        }
                        startSmsCountdown(sendSmsButton);
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }

    private void sendSmsCode(TextInputEditText phoneInput, Button smsButton) {
        String phone = textOf(phoneInput);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
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
                            toast("验证码已发送（演示：" + code + "）");
                        } else {
                            toast("验证码已发送");
                        }
                        startSmsCountdown(smsButton);
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
                                      TextInputEditText newPasswordInput,
                                      TextInputEditText confirmPasswordInput) {
        String phone = textOf(phoneInput);
        String code = textOf(codeInput);
        String newPassword = textOf(newPasswordInput);
        String confirmPassword = textOf(confirmPasswordInput);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
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
        if (!newPassword.equals(confirmPassword)) {
            toast("两次密码输入不一致");
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

    private void startSmsCountdown(Button smsButton) {
        cancelSmsCountdown();
        smsButton.setEnabled(false);
        smsCountdownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                smsButton.setText((millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                smsButton.setEnabled(true);
                smsButton.setText("获取验证码");
            }
        };
        smsCountdownTimer.start();
    }

    private void cancelSmsCountdown() {
        if (smsCountdownTimer != null) {
            smsCountdownTimer.cancel();
            smsCountdownTimer = null;
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
            String smsCode = textOf(smsCodeInput);
            String confirm = textOf(confirmInput);
            if (userName.isEmpty() || phone.isEmpty()) {
                toast("请填写用户名和手机号");
                return;
            }
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                toast("请输入正确手机号");
                return;
            }
            if (smsCode.isEmpty()) {
                toast("请先获取并输入验证码");
                return;
            }
            if (!smsCode.equals(correctSmsCode)) {
                toast("验证码错误");
                return;
            }
            if (password.length() < 6) {
                toast("密码至少 6 位");
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
