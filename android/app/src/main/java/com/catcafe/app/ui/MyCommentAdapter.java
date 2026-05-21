package com.catcafe.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.model.CommentDetail;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.VH> {
    public interface OnOpenCommentListener {
        void onOpen(CommentDetail comment);
    }

    public interface OnDeleteCommentListener {
        void onDelete(CommentDetail comment);
    }

    private final List<CommentDetail> items = new ArrayList<>();
    private final OnOpenCommentListener openListener;
    private final OnDeleteCommentListener deleteListener;

    public MyCommentAdapter(List<CommentDetail> data, OnOpenCommentListener openListener, OnDeleteCommentListener deleteListener) {
        if (data != null) {
            items.addAll(data);
        }
        this.openListener = openListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_comment, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CommentDetail comment = items.get(position);
        holder.target.setText(targetLabel(comment));
        holder.content.setText(comment.content == null ? "" : comment.content);
        holder.meta.setText(statusLabel(comment.auditStatus) + " · " + safeTime(comment.publishTime));
        holder.itemView.setOnClickListener(v -> openListener.onOpen(comment));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(comment));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String targetLabel(CommentDetail comment) {
        String type = comment.targetType == 0 ? "商品" : "猫咪";
        return type + " #" + comment.targetId;
    }

    private String statusLabel(int status) {
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

    private String safeTime(String value) {
        return value == null ? "" : value;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView target;
        TextView content;
        TextView meta;
        MaterialButton deleteButton;

        VH(@NonNull View itemView) {
            super(itemView);
            target = itemView.findViewById(R.id.myCommentTarget);
            content = itemView.findViewById(R.id.myCommentContent);
            meta = itemView.findViewById(R.id.myCommentMeta);
            deleteButton = itemView.findViewById(R.id.myCommentDeleteButton);
        }
    }
}
