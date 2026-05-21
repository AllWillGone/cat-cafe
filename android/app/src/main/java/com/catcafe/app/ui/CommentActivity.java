package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.catcafe.app.R;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.CommentCreateRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CommentActivity extends BaseToolbarActivity {
    private static final String EXTRA_TARGET_TYPE = "target_type";
    private static final String EXTRA_TARGET_ID = "target_id";
    private static final String EXTRA_TARGET_NAME = "target_name";

    public static Intent intent(Context context, int targetType, long targetId, String targetName) {
        return new Intent(context, CommentActivity.class)
                .putExtra(EXTRA_TARGET_TYPE, targetType)
                .putExtra(EXTRA_TARGET_ID, targetId)
                .putExtra(EXTRA_TARGET_NAME, targetName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        setupToolbar(R.id.commentToolbar);
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录后评论", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        int targetType = getIntent().getIntExtra(EXTRA_TARGET_TYPE, 0);
        long targetId = getIntent().getLongExtra(EXTRA_TARGET_ID, 0L);
        String targetName = getIntent().getStringExtra(EXTRA_TARGET_NAME);
        if (targetName != null) {
            getSupportActionBar().setTitle(targetName);
        }

        TextInputEditText contentInput = findViewById(R.id.commentContentInput);
        MaterialButton submitButton = findViewById(R.id.commentSubmitButton);
        submitButton.setOnClickListener(v -> {
            String content = contentInput.getText() == null ? "" : contentInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
                return;
            }
            NetworkHelper.enqueue(this,
                    ApiClient.getService(this).createComment(new CommentCreateRequest(targetType, targetId, content)),
                    new ApiCallback<com.catcafe.app.model.CommentDetail>() {
                        @Override
                        public void onSuccess(com.catcafe.app.model.CommentDetail data) {
                            Toast.makeText(CommentActivity.this, "评论发表成功，等待审核", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(CommentActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
