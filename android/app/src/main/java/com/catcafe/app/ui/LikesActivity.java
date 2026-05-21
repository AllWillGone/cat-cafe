package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.model.LikeDetail;
import com.catcafe.app.model.PaginatedLikes;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;

public class LikesActivity extends BaseToolbarActivity {
    public static Intent intent(Context context) {
        return new Intent(context, LikesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);
        setupToolbar(R.id.likeToolbar);
        RecyclerView list = findViewById(R.id.likeList);
        list.setLayoutManager(new LinearLayoutManager(this));

        NetworkHelper.enqueue(this, ApiClient.getService(this).getLikes(null, null, 0, 50), new ApiCallback<PaginatedLikes>() {
            @Override
            public void onSuccess(PaginatedLikes data) {
                list.setAdapter(new LikeAdapter(data.items, LikesActivity.this::cancelLike, LikesActivity.this::openLikeTarget));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LikesActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelLike(LikeDetail like) {
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).deleteLike(like.likeId),
                new ApiCallback<java.util.Map<String, Object>>() {
                    @Override
                    public void onSuccess(java.util.Map<String, Object> result) {
                        Toast.makeText(LikesActivity.this, "已取消点赞", Toast.LENGTH_SHORT).show();
                        recreate();
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
}
