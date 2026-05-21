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

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.PaginatedComments;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;

public class ProductDetailActivity extends BaseDetailActivity {
    private static final String EXTRA_ID = "product_id";

    private long productId;
    private RecyclerView commentList;
    private TextView commentEmpty;

    public static Intent intent(Context context, long id) {
        return new Intent(context, ProductDetailActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setupToolbar(R.id.productDetailToolbar);

        productId = getIntent().getLongExtra(EXTRA_ID, 0L);
        ImageView image = findViewById(R.id.productDetailImage);
        TextView name = findViewById(R.id.productDetailName);
        TextView meta = findViewById(R.id.productDetailMeta);
        TextView price = findViewById(R.id.productDetailPrice);
        TextView desc = findViewById(R.id.productDetailDesc);
        TextView stock = findViewById(R.id.productDetailStock);
        TextView likes = findViewById(R.id.productDetailLikes);
        MaterialButton addButton = findViewById(R.id.productDetailAddButton);
        MaterialButton commentButton = findViewById(R.id.productDetailCommentButton);
        ImageButton likeButton = findViewById(R.id.productDetailLikeButton);
        commentList = findViewById(R.id.productDetailCommentList);
        commentEmpty = findViewById(R.id.productDetailCommentEmpty);
        commentList.setLayoutManager(new LinearLayoutManager(this));

        NetworkHelper.enqueue(this, ApiClient.getService(this).getProduct(productId), new ApiCallback<ProductDetail>() {
            @Override
            public void onSuccess(ProductDetail data) {
                name.setText(data.productName);
                meta.setText(UiText.productCategory(data.category) + " · " + UiText.productStatus(data.status));
                price.setText(UiText.productPrice(data));
                desc.setText(data.description == null ? "暂无说明" : data.description);
                stock.setText("库存 " + data.stockQuantity);
                likes.setText(String.valueOf(data.likeCount));
                Glide.with(ProductDetailActivity.this)
                        .load(AppConfig.buildImageUrl(data.imageUrl))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(image);
                addButton.setOnClickListener(v -> {
                    if (sessionManager.isAdmin()) {
                        Toast.makeText(ProductDetailActivity.this, "管理员账号不参与顾客操作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!sessionManager.isLoggedIn()) {
                        requireLogin("请先登录后购买");
                        return;
                    }
                    cart().add(data, 1);
                    Toast.makeText(ProductDetailActivity.this, "已加入购物车", Toast.LENGTH_SHORT).show();
                });
                commentButton.setOnClickListener(v -> {
                    if (sessionManager.isAdmin()) {
                        Toast.makeText(ProductDetailActivity.this, "管理员账号不参与顾客操作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!sessionManager.isLoggedIn()) {
                        requireLogin("请先登录后评论");
                        return;
                    }
                    startActivity(CommentActivity.intent(ProductDetailActivity.this, 0, data.productId, data.productName));
                });
                if (sessionManager.isAdmin()) {
                    likeButton.setVisibility(View.GONE);
                } else {
                    new LikeController(ProductDetailActivity.this, 0, data.productId, data.likeCount, likes, likeButton).bind();
                }
                loadComments();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productId > 0 && commentList != null) {
            loadComments();
        }
    }

    private void loadComments() {
        NetworkHelper.enqueue(this, ApiClient.getService(this).getComments(0, productId, 0, 20), new ApiCallback<PaginatedComments>() {
            @Override
            public void onSuccess(PaginatedComments data) {
                boolean empty = data.items == null || data.items.isEmpty();
                commentEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                commentList.setAdapter(new CommentAdapter(data.items));
            }

            @Override
            public void onError(String message) {
                commentEmpty.setVisibility(View.VISIBLE);
                commentEmpty.setText(message);
            }
        });
    }
}
