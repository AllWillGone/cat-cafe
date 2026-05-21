package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.CommentAuditRequest;
import com.catcafe.app.model.CommentDetail;
import com.catcafe.app.model.PaginatedComments;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Map;

public class AdminCommentsActivity extends AdminListActivityBase {
    private final String[] filters = {"全部评论", "待审核", "已通过", "已拒绝"};
    private AdminManageAdapter<CommentDetail> adapter;

    public static Intent intent(Context context) {
        return new Intent(context, AdminCommentsActivity.class);
    }

    @Override
    protected void configure() {
        setTitleText("评论管理");
        keywordLayout.setHint("用户名、评论ID、内容搜索");
        setFilterLabels(filters);
        adapter = new AdminManageAdapter<>(this::bindComment);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        Integer status = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).adminGetComments(status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedComments>() {
                    @Override
                    public void onSuccess(PaginatedComments data) {
                        showLoading(false);
                        updatePaging(data.total);
                        adapter.submitList(data.items);
                    }

                    @Override
                    public void onError(String message) {
                        showLoading(false);
                        toast(message);
                    }
                });
    }

    private void bindComment(AdminManageAdapter.VH holder, CommentDetail comment) {
        holder.title.setText("#" + comment.commentId + " · " + statusText(comment.auditStatus));
        holder.meta.setText((comment.userName == null ? "未知用户" : comment.userName) + " · " + targetText(comment));
        holder.body.setText(comment.content);
        holder.footer.setText(comment.publishTime == null ? "" : comment.publishTime);

        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("通过");
        holder.primaryButton.setEnabled(comment.auditStatus != 1);
        holder.primaryButton.setOnClickListener(v -> audit(comment.commentId, 1));

        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText("拒绝");
        holder.secondaryButton.setEnabled(comment.auditStatus != 2);
        holder.secondaryButton.setOnClickListener(v -> audit(comment.commentId, 2));

        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDelete(comment));
    }

    private void audit(long commentId, int status) {
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).adminAuditComment(commentId, new CommentAuditRequest(status)),
                new ApiCallback<CommentDetail>() {
                    @Override
                    public void onSuccess(CommentDetail data) {
                        toast(status == 1 ? "评论已通过" : "评论已拒绝");
                        loadData();
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }

    private void confirmDelete(CommentDetail comment) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除评论")
                .setMessage("确认删除这条评论？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteComment(comment.commentId))
                .show();
    }

    private void deleteComment(long commentId) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).deleteComment(commentId), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                toast("评论已删除");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private String statusText(int status) {
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

    private String targetText(CommentDetail comment) {
        return (comment.targetType == 0 ? "商品 #" : "猫咪 #") + comment.targetId;
    }
}
