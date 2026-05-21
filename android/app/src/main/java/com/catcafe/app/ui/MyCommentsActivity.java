package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class MyCommentsActivity extends BaseToolbarActivity {
    private RecyclerView list;
    private TextView empty;

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
        list.setLayoutManager(new LinearLayoutManager(this));
        loadComments();
    }

    private void loadComments() {
        NetworkHelper.enqueue(this, ApiClient.getService(this).getMyComments(null, 0, 50), new ApiCallback<PaginatedComments>() {
            @Override
            public void onSuccess(PaginatedComments data) {
                boolean isEmpty = data.items == null || data.items.isEmpty();
                empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                list.setAdapter(new MyCommentAdapter(data.items, MyCommentsActivity.this::openTarget, MyCommentsActivity.this::deleteComment));
            }

            @Override
            public void onError(String message) {
                empty.setVisibility(View.VISIBLE);
                empty.setText(message);
            }
        });
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
}
