package com.catcafe.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.model.CommentDetail;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {
    private final List<CommentDetail> items = new ArrayList<>();

    public CommentAdapter(List<CommentDetail> data) {
        if (data != null) {
            items.addAll(data);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CommentDetail comment = items.get(position);
        holder.author.setText(comment.userName == null || comment.userName.trim().isEmpty() ? "用户" : comment.userName);
        holder.content.setText(comment.content == null ? "" : comment.content);
        holder.meta.setText(comment.publishTime == null ? "" : comment.publishTime);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView author;
        TextView content;
        TextView meta;

        VH(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.commentAuthor);
            content = itemView.findViewById(R.id.commentContent);
            meta = itemView.findViewById(R.id.commentMeta);
        }
    }
}
