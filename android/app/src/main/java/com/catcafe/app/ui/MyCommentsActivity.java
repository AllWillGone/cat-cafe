package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.model.CommentDetail;
import com.catcafe.app.model.PaginatedComments;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MyCommentsActivity extends BaseToolbarActivity {
    private final String[] typeLabels = {"全部评论", "商品", "猫咪"};
    private RecyclerView list;
    private TextView empty;
    private Spinner typeFilter;
    private TextInputEditText keywordInput;

    public static Intent intent(Context context) {
        return new Intent(context, MyCommentsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);
        setupToolbar(R.id.commentListToolbar);
        list = findViewById(R.id.commentList);
        empty = findViewById(R.id.commentListEmpty);
        typeFilter = findViewById(R.id.commentTypeFilter);
        keywordInput = findViewById(R.id.commentKeywordInput);
        MaterialButton searchButton = findViewById(R.id.commentSearchButton);

        list.setLayoutManager(new LinearLayoutManager(this));
        typeFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeLabels));
        searchButton.setOnClickListener(v -> loadComments());
        loadComments();
    }

    private void loadComments() {
        NetworkHelper.enqueue(this, ApiClient.getService(this).getMyComments(selectedTargetType(), 0, 100), new ApiCallback<PaginatedComments>() {
            @Override
            public void onSuccess(PaginatedComments data) {
                List<CommentDetail> items = filterByKeyword(data.items);
                boolean isEmpty = items.isEmpty();
                empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                list.setAdapter(new MyCommentAdapter(items, MyCommentsActivity.this::openTarget, MyCommentsActivity.this::deleteComment));
            }

            @Override
            public void onError(String message) {
                empty.setVisibility(View.VISIBLE);
                empty.setText(message);
            }
        });
    }

    private Integer selectedTargetType() {
        int position = typeFilter.getSelectedItemPosition();
        if (position == 0) {
            return null;
        }
        return position - 1;
    }

    private List<CommentDetail> filterByKeyword(List<CommentDetail> source) {
        List<CommentDetail> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        String keyword = textOf(keywordInput).toLowerCase();
        if (keyword.isEmpty()) {
            result.addAll(source);
            return result;
        }
        for (CommentDetail comment : source) {
            if (matchesKeyword(comment, keyword)) {
                result.add(comment);
            }
        }
        return result;
    }

    private boolean matchesKeyword(CommentDetail comment, String keyword) {
        return contains(comment.content, keyword)
                || contains(comment.userName, keyword)
                || contains(String.valueOf(comment.targetId), keyword)
                || contains(comment.targetType == 0 ? "商品" : "猫咪", keyword)
                || contains(statusLabel(comment.auditStatus), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void openTarget(CommentDetail comment) {
        if (comment.targetType == 0) {
            startActivity(ProductDetailActivity.intent(this, comment.targetId));
        } else if (comment.targetType == 1) {
            startActivity(CatDetailActivity.intent(this, comment.targetId));
        } else {
            Toast.makeText(this, "不支持的目标类型", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteComment(CommentDetail comment) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).deleteComment(comment.commentId), new ApiCallback<java.util.Map<String, Object>>() {
            @Override
            public void onSuccess(java.util.Map<String, Object> result) {
                Toast.makeText(MyCommentsActivity.this, "评论已删除", Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyCommentsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String statusLabel(int status) {
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
