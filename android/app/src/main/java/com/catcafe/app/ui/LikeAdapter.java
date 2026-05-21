package com.catcafe.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.model.LikeDetail;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class LikeAdapter extends RecyclerView.Adapter<LikeAdapter.VH> {
    public interface OnCancelLikeListener {
        void onCancel(LikeDetail like);
    }

    public interface OnOpenLikeListener {
        void onOpen(LikeDetail like);
    }

    private final List<LikeDetail> items = new ArrayList<>();
    private final OnCancelLikeListener cancelListener;
    private final OnOpenLikeListener openListener;

    public LikeAdapter(List<LikeDetail> data, OnCancelLikeListener cancelListener, OnOpenLikeListener openListener) {
        if (data != null) {
            items.addAll(data);
        }
        this.cancelListener = cancelListener;
        this.openListener = openListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_like, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        LikeDetail like = items.get(position);
        holder.title.setText(UiText.likeLabel(like));
        holder.meta.setText(UiText.likeType(like.likeType));
        holder.itemView.setOnClickListener(v -> openListener.onOpen(like));
        holder.cancelButton.setOnClickListener(v -> cancelListener.onCancel(like));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        MaterialButton cancelButton;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.likeTitle);
            meta = itemView.findViewById(R.id.likeMeta);
            cancelButton = itemView.findViewById(R.id.likeCancelButton);
        }
    }
}
