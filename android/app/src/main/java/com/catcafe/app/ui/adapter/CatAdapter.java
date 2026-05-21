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
import com.catcafe.app.model.CatDetail;
import com.catcafe.app.util.UiText;

import java.util.ArrayList;
import java.util.List;

public class CatAdapter extends RecyclerView.Adapter<CatAdapter.VH> {
    public interface OnCatClickListener {
        void onCatClick(CatDetail cat);
    }

    public interface OnCatLikeBindListener {
        void onBind(CatDetail cat, TextView countView, ImageButton button);
    }

    private final Context context;
    private final OnCatClickListener listener;
    private final OnCatLikeBindListener likeBindListener;
    private final List<CatDetail> items = new ArrayList<>();

    public CatAdapter(Context context, OnCatClickListener listener) {
        this(context, listener, null);
    }

    public CatAdapter(Context context, OnCatClickListener listener, OnCatLikeBindListener likeBindListener) {
        this.context = context;
        this.listener = listener;
        this.likeBindListener = likeBindListener;
    }

    public void submitList(List<CatDetail> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cat, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CatDetail cat = items.get(position);
        holder.name.setText(cat.catName);
        holder.detail.setText(UiText.safeJoin(cat.breed, UiText.catStatus(cat.status), " · "));
        holder.likeCount.setText(String.valueOf(cat.likeCount));
        holder.likeButton.setOnClickListener(null);
        if (likeBindListener != null) {
            likeBindListener.onBind(cat, holder.likeCount, holder.likeButton);
        }
        Glide.with(context)
                .load(AppConfig.buildImageUrl(cat.photoUrl))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.image);
        holder.itemView.setOnClickListener(v -> listener.onCatClick(cat));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView detail;
        TextView likeCount;
        ImageButton likeButton;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.catImage);
            name = itemView.findViewById(R.id.catName);
            detail = itemView.findViewById(R.id.catDetail);
            likeCount = itemView.findViewById(R.id.catLikeCount);
            likeButton = itemView.findViewById(R.id.catLikeButton);
        }
    }
}
