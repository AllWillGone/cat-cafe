package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.CatDetail;
import com.catcafe.app.model.PaginatedComments;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;

public class CatDetailActivity extends BaseToolbarActivity {
    private static final String EXTRA_ID = "cat_id";

    private SessionManager sessionManager;
    private long catId;
    private SwipeRefreshLayout refreshLayout;
    private ImageView image;
    private TextView name;
    private TextView meta;
    private TextView personality;
    private TextView notes;
    private TextView likes;
    private MaterialButton commentButton;
    private ImageButton likeButton;
    private RecyclerView commentList;
    private TextView commentEmpty;
    private int refreshRequestsPending;
    private boolean hasResumedOnce;

    public static Intent intent(Context context, long id) {
        return new Intent(context, CatDetailActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_detail);
        setupToolbar(R.id.catDetailToolbar);
        sessionManager = new SessionManager(this);

        catId = getIntent().getLongExtra(EXTRA_ID, 0L);
        refreshLayout = findViewById(R.id.catDetailRefresh);
        image = findViewById(R.id.catDetailImage);
        name = findViewById(R.id.catDetailName);
        meta = findViewById(R.id.catDetailMeta);
        personality = findViewById(R.id.catDetailPersonality);
        notes = findViewById(R.id.catDetailNotes);
        likes = findViewById(R.id.catDetailLikes);
        commentButton = findViewById(R.id.catDetailCommentButton);
        likeButton = findViewById(R.id.catDetailLikeButton);
        commentList = findViewById(R.id.catDetailCommentList);
        commentEmpty = findViewById(R.id.catDetailCommentEmpty);
        commentList.setLayoutManager(new LinearLayoutManager(this));
        refreshLayout.setOnRefreshListener(this::refreshDetailPage);

        refreshDetailPage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasResumedOnce && catId > 0 && commentList != null) {
            loadComments(false);
        }
        hasResumedOnce = true;
    }

    private void refreshDetailPage() {
        refreshLayout.setRefreshing(true);
        refreshRequestsPending = 2;
        loadCat(true);
        loadComments(true);
    }

    private void loadCat(boolean trackRefresh) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).getCat(catId), new ApiCallback<CatDetail>() {
            @Override
            public void onSuccess(CatDetail data) {
                finishRefreshRequest(trackRefresh);
                name.setText(data.catName);
                meta.setText(UiText.safeJoin(data.breed, UiText.catStatus(data.status), " · "));
                personality.setText(data.personality);
                notes.setText(data.notes);
                likes.setText(String.valueOf(data.likeCount));
                Glide.with(CatDetailActivity.this)
                        .load(AppConfig.buildImageUrl(data.photoUrl))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(image);
                commentButton.setOnClickListener(v -> {
                    if (sessionManager.isAdmin()) {
                        Toast.makeText(CatDetailActivity.this, "管理员账号不参与顾客操作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!sessionManager.isLoggedIn()) {
                        Toast.makeText(CatDetailActivity.this, "请先登录后评论", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CatDetailActivity.this, AuthActivity.class));
                        return;
                    }
                    startActivity(CommentActivity.intent(CatDetailActivity.this, 1, data.catId, data.catName));
                });
                if (sessionManager.isAdmin()) {
                    likeButton.setVisibility(View.GONE);
                } else {
                    new LikeController(CatDetailActivity.this, 2, data.catId, data.likeCount, likes, likeButton).bind();
                }
            }

            @Override
            public void onError(String message) {
                finishRefreshRequest(trackRefresh);
                Toast.makeText(CatDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments(boolean trackRefresh) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).getComments(1, catId, "likeCount", 0, 20), new ApiCallback<PaginatedComments>() {
            @Override
            public void onSuccess(PaginatedComments data) {
                finishRefreshRequest(trackRefresh);
                boolean empty = data.items == null || data.items.isEmpty();
                commentEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                commentList.setAdapter(new CommentAdapter(CatDetailActivity.this, data.items));
            }

            @Override
            public void onError(String message) {
                finishRefreshRequest(trackRefresh);
                commentEmpty.setVisibility(View.VISIBLE);
                commentEmpty.setText(message);
            }
        });
    }

    private void finishRefreshRequest(boolean trackRefresh) {
        if (!trackRefresh || refreshLayout == null) {
            return;
        }
        refreshRequestsPending = Math.max(0, refreshRequestsPending - 1);
        if (refreshRequestsPending == 0) {
            refreshLayout.setRefreshing(false);
        }
    }
}
