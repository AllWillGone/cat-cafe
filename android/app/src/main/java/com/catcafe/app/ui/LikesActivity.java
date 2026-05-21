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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.catcafe.app.R;
import com.catcafe.app.model.LikeDetail;
import com.catcafe.app.model.PaginatedLikes;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LikesActivity extends BaseToolbarActivity {
    private final String[] typeLabels = {"全部点赞", "商品", "评论", "猫咪"};
    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;
    private TextView empty;
    private Spinner typeFilter;
    private TextInputEditText keywordInput;

    public static Intent intent(Context context) {
        return new Intent(context, LikesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);
        setupToolbar(R.id.likeToolbar);
        list = findViewById(R.id.likeList);
        refreshLayout = findViewById(R.id.likeRefresh);
        empty = findViewById(R.id.likeListEmpty);
        typeFilter = findViewById(R.id.likeTypeFilter);
        keywordInput = findViewById(R.id.likeKeywordInput);
        MaterialButton searchButton = findViewById(R.id.likeSearchButton);

        list.setLayoutManager(new LinearLayoutManager(this));
        refreshLayout.setOnRefreshListener(this::loadLikes);
        typeFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeLabels));
        searchButton.setOnClickListener(v -> loadLikes());
        loadLikes();
    }

    private void loadLikes() {
        Integer likeType = selectedLikeType();
        String keyword = textOf(keywordInput);
        if (keyword.isEmpty()) {
            keyword = null;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).getLikes(likeType, keyword, 0, 50), new ApiCallback<PaginatedLikes>() {
            @Override
            public void onSuccess(PaginatedLikes data) {
                refreshLayout.setRefreshing(false);
                boolean isEmpty = data.items == null || data.items.isEmpty();
                empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                list.setAdapter(new LikeAdapter(data.items, LikesActivity.this::cancelLike, LikesActivity.this::openLikeTarget));
            }

            @Override
            public void onError(String message) {
                refreshLayout.setRefreshing(false);
                Toast.makeText(LikesActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer selectedLikeType() {
        int position = typeFilter.getSelectedItemPosition();
        if (position == 0) {
            return null;
        }
        if (position == 1) {
            return 0;
        }
        if (position == 2) {
            return 1;
        }
        return 2;
    }

    private void cancelLike(LikeDetail like) {
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).deleteLike(like.likeId),
                new ApiCallback<java.util.Map<String, Object>>() {
                    @Override
                    public void onSuccess(java.util.Map<String, Object> result) {
                        Toast.makeText(LikesActivity.this, "已取消点赞", Toast.LENGTH_SHORT).show();
                        loadLikes();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(LikesActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openLikeTarget(LikeDetail like) {
        if (like.likeType == 0) {
            startActivity(ProductDetailActivity.intent(this, like.objectId));
            return;
        }
        if (like.likeType == 2) {
            startActivity(CatDetailActivity.intent(this, like.objectId));
            return;
        }
        if (like.likeType == 1 && like.targetType != null && like.targetId != null) {
            openTarget(like.targetType, like.targetId);
            return;
        }
        Toast.makeText(this, "目标内容不存在或已删除", Toast.LENGTH_SHORT).show();
    }

    private void openTarget(int targetType, long targetId) {
        if (targetType == 0) {
            startActivity(ProductDetailActivity.intent(this, targetId));
        } else if (targetType == 1) {
            startActivity(CatDetailActivity.intent(this, targetId));
        } else {
            Toast.makeText(this, "不支持的目标类型", Toast.LENGTH_SHORT).show();
        }
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
