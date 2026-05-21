package com.catcafe.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.catcafe.app.R;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.LikeCreateRequest;
import com.catcafe.app.model.LikeDetail;
import com.catcafe.app.model.PaginatedLikes;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;

public class LikeController {
    private final Activity activity;
    private final int likeType;
    private final long objectId;
    private final TextView countView;
    private final ImageButton button;
    private final SessionManager sessionManager;

    private int count;
    private boolean liked;
    private long likeId;
    private boolean loading;

    public LikeController(Activity activity, int likeType, long objectId, int initialCount,
                          TextView countView, ImageButton button) {
        this.activity = activity;
        this.likeType = likeType;
        this.objectId = objectId;
        this.count = Math.max(0, initialCount);
        this.countView = countView;
        this.button = button;
        this.sessionManager = new SessionManager(activity);
    }

    public void bind() {
        render();
        button.setOnClickListener(v -> toggle());
        loadMyLikeState();
    }

    private void loadMyLikeState() {
        if (!sessionManager.isLoggedIn()) {
            liked = false;
            likeId = 0L;
            render();
            return;
        }
        NetworkHelper.enqueue(activity,
                ApiClient.getService(activity).getLikes(likeType, null, 0, 200),
                new ApiCallback<PaginatedLikes>() {
                    @Override
                    public void onSuccess(PaginatedLikes data) {
                        liked = false;
                        likeId = 0L;
                        if (data.items != null) {
                            for (LikeDetail like : data.items) {
                                if (like.objectId == objectId) {
                                    liked = true;
                                    likeId = like.likeId;
                                    break;
                                }
                            }
                        }
                        render();
                    }

                    @Override
                    public void onError(String message) {
                        render();
                    }
                });
    }

    private void toggle() {
        if (loading) {
            return;
        }
        if (!sessionManager.isLoggedIn()) {
            activity.startActivity(new Intent(activity, AuthActivity.class));
            return;
        }
        if (liked) {
            unlike();
        } else {
            like();
        }
    }

    private void like() {
        loading = true;
        render();
        NetworkHelper.enqueue(activity,
                ApiClient.getService(activity).createLike(new LikeCreateRequest(likeType, objectId, null)),
                new ApiCallback<LikeDetail>() {
                    @Override
                    public void onSuccess(LikeDetail data) {
                        liked = true;
                        likeId = data.likeId;
                        count += 1;
                        loading = false;
                        render();
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        loadMyLikeState();
                    }
                });
    }

    private void unlike() {
        if (likeId <= 0L) {
            loadMyLikeState();
            return;
        }
        loading = true;
        render();
        NetworkHelper.enqueue(activity,
                ApiClient.getService(activity).deleteLike(likeId),
                new ApiCallback<java.util.Map<String, Object>>() {
                    @Override
                    public void onSuccess(java.util.Map<String, Object> data) {
                        liked = false;
                        likeId = 0L;
                        count = Math.max(0, count - 1);
                        loading = false;
                        render();
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        loadMyLikeState();
                    }
                });
    }

    private void render() {
        countView.setText(String.valueOf(count));
        button.setImageResource(liked ? R.drawable.ic_heart_filled_24 : R.drawable.ic_heart_outline_24);
        button.setEnabled(!loading);
        button.setAlpha(loading ? 0.5f : 1f);
        button.setContentDescription(liked ? "取消点赞" : "点赞");
    }
}
