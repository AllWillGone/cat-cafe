package com.catcafe.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.ui.ProductRules;
import com.catcafe.app.util.UiText;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {
    public interface OnProductClickListener {
        void onProductClick(ProductDetail product);
    }

    public interface OnCartQuantityChangeListener {
        int getQuantity(ProductDetail product);

        void onIncrease(ProductDetail product);

        void onDecrease(ProductDetail product);
    }

    public interface OnProductLikeBindListener {
        void onBind(ProductDetail product, TextView countView, ImageButton button);
    }

    private final Context context;
    private final OnProductClickListener productClickListener;
    private final OnCartQuantityChangeListener cartQuantityChangeListener;
    private final OnProductLikeBindListener likeBindListener;
    private final List<ProductDetail> items = new ArrayList<>();

    public ProductAdapter(Context context, OnProductClickListener productClickListener,
                          OnCartQuantityChangeListener cartQuantityChangeListener) {
        this(context, productClickListener, cartQuantityChangeListener, null);
    }

    public ProductAdapter(Context context, OnProductClickListener productClickListener,
                          OnCartQuantityChangeListener cartQuantityChangeListener,
                          OnProductLikeBindListener likeBindListener) {
        this.context = context;
        this.productClickListener = productClickListener;
        this.cartQuantityChangeListener = cartQuantityChangeListener;
        this.likeBindListener = likeBindListener;
    }

    public void submitList(List<ProductDetail> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void refreshQuantities() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ProductDetail product = items.get(position);
        boolean catCafeTicket = ProductRules.isCatCafeTicket(product);
        int quantity = cartQuantityChangeListener.getQuantity(product);

        holder.name.setText(product.productName);
        holder.meta.setText(UiText.safeJoin(UiText.productCategory(product.category), UiText.productStatus(product.status), " · "));
        holder.price.setText(UiText.productPrice(product));
        holder.stock.setText("库存 " + product.stockQuantity);
        holder.likeCount.setText(String.valueOf(product.likeCount));
        holder.quantityText.setText(String.valueOf(quantity));

        holder.image.setVisibility(catCafeTicket ? View.GONE : View.VISIBLE);
        holder.meta.setVisibility(catCafeTicket ? View.GONE : View.VISIBLE);
        holder.stock.setVisibility(catCafeTicket ? View.GONE : View.VISIBLE);
        holder.likeRow.setVisibility(catCafeTicket ? View.GONE : View.VISIBLE);
        holder.root.setMinimumHeight(catCafeTicket ? dp(48) : dp(64));
        holder.root.setPadding(dp(8), catCafeTicket ? dp(4) : dp(8), dp(8), catCafeTicket ? dp(4) : dp(8));
        ViewGroup.MarginLayoutParams textParams = (ViewGroup.MarginLayoutParams) holder.textColumn.getLayoutParams();
        textParams.setMarginStart(catCafeTicket ? 0 : dp(10));
        holder.textColumn.setLayoutParams(textParams);

        if (!catCafeTicket) {
            Glide.with(context)
                    .load(AppConfig.buildImageUrl(product.imageUrl))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.image);
        } else {
            Glide.with(context).clear(holder.image);
        }

        holder.increaseButton.setOnClickListener(v -> {
            cartQuantityChangeListener.onIncrease(product);
            notifyItemChanged(holder.getBindingAdapterPosition());
        });
        holder.decreaseButton.setOnClickListener(v -> {
            cartQuantityChangeListener.onDecrease(product);
            notifyItemChanged(holder.getBindingAdapterPosition());
        });
        holder.decreaseButton.setEnabled(quantity > 0);
        holder.decreaseButton.setAlpha(quantity > 0 ? 1f : 0.35f);

        holder.likeButton.setOnClickListener(null);
        if (likeBindListener != null) {
            likeBindListener.onBind(product, holder.likeCount, holder.likeButton);
        }
        holder.itemView.setOnClickListener(v -> productClickListener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static class VH extends RecyclerView.ViewHolder {
        View root;
        ImageView image;
        View textColumn;
        TextView name;
        TextView meta;
        TextView price;
        TextView stock;
        View likeRow;
        TextView likeCount;
        ImageButton likeButton;
        ImageButton decreaseButton;
        TextView quantityText;
        ImageButton increaseButton;

        VH(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.productRoot);
            image = itemView.findViewById(R.id.productImage);
            textColumn = itemView.findViewById(R.id.productTextColumn);
            name = itemView.findViewById(R.id.productName);
            meta = itemView.findViewById(R.id.productMeta);
            price = itemView.findViewById(R.id.productPrice);
            stock = itemView.findViewById(R.id.productStock);
            likeRow = itemView.findViewById(R.id.productLikeRow);
            likeCount = itemView.findViewById(R.id.productLikeCount);
            likeButton = itemView.findViewById(R.id.productLikeButton);
            decreaseButton = itemView.findViewById(R.id.decreaseCartButton);
            quantityText = itemView.findViewById(R.id.cartQuantityText);
            increaseButton = itemView.findViewById(R.id.increaseCartButton);
        }
    }
}
