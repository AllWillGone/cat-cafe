package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.catcafe.app.R;
import com.catcafe.app.model.ChangePasswordRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PasswordActivity extends BaseToolbarActivity {
    public static Intent intent(Context context) {
        return new Intent(context, PasswordActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        setupToolbar(R.id.passwordToolbar);

        TextInputEditText oldInput = findViewById(R.id.passwordOldInput);
        TextInputEditText newInput = findViewById(R.id.passwordNewInput);
        MaterialButton submitButton = findViewById(R.id.passwordSubmitButton);

        submitButton.setOnClickListener(v -> NetworkHelper.enqueue(this,
                ApiClient.getService(this).changePassword(new ChangePasswordRequest(textOf(oldInput), textOf(newInput))),
                new ApiCallback<java.util.Map<String, Object>>() {
                    @Override
                    public void onSuccess(java.util.Map<String, Object> data) {
                        Toast.makeText(PasswordActivity.this, "密码已修改", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
