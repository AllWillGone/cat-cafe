package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.BatchOrderResponse;
import com.catcafe.app.model.OrderDetailItem;
import com.catcafe.app.model.OrderUpdateRequest;
import com.catcafe.app.model.PaginatedOrders;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminOrdersActivity extends AdminListActivityBase {
    private final String[] filters = {"全部订单", "未支付", "已支付", "待取货", "已完成", "已取消"};
    private final String[] statusOptions = {"未支付", "已支付", "待取货", "已完成", "已取消"};
    private final String[] statusDescriptions = {
            "客户已下单，等待支付",
            "已完成付款，等待备货",
            "商品已备好，等待取货",
            "订单已完成，可归档",
            "订单已取消，库存已处理"
    };
    private AdminManageAdapter<BatchOrderResponse> adapter;

    public static Intent intent(Context context) {
        return new Intent(context, AdminOrdersActivity.class);
    }

    @Override
    protected void configure() {
        setTitleText("订单管理");
        keywordLayout.setHint("批次号或联系人搜索");
        setFilterLabels(filters);
        adapter = new AdminManageAdapter<>(this::bindOrder);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        Integer status = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).adminGetOrders(status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedOrders>() {
                    @Override
                    public void onSuccess(PaginatedOrders data) {
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

    private void bindOrder(AdminManageAdapter.VH holder, BatchOrderResponse order) {
        holder.title.setText(UiText.batchLabel(order));
        holder.meta.setText(UiText.orderStatus(order.orderStatus) + " · " + UiText.paymentMethod(order.paymentMethod));
        holder.body.setText(productSummary(order));
        holder.footer.setText(order.userName + " · " + order.userPhone + "\n总计 " + UiText.price(order.totalAmount));

        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("详情");
        holder.primaryButton.setOnClickListener(v -> showDetail(order));

        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText("状态");
        holder.secondaryButton.setOnClickListener(v -> showStatusDialog(order));

        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDelete(order));
    }

    private void showDetail(BatchOrderResponse order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(UiText.batchLabel(order))
                .setMessage(detailText(order))
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showStatusDialog(BatchOrderResponse order) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        content.setPadding(padding, 0, padding, 0);

        TextView summary = new TextView(this);
        summary.setText(UiText.batchLabel(order) + "\n" + order.userName + " · " + UiText.price(order.totalAmount));
        summary.setTextColor(getColor(R.color.cat_muted));
        summary.setTextSize(13);
        summary.setLineSpacing(dp(2), 1f);
        content.addView(summary);

        final int[] selectedStatus = {Math.max(0, Math.min(order.orderStatus, statusOptions.length - 1))};
        List<MaterialCardView> cards = new ArrayList<>();
        List<RadioButton> radioButtons = new ArrayList<>();
        for (int i = 0; i < statusOptions.length; i++) {
            content.addView(createStatusCard(i, selectedStatus, cards, radioButtons));
        }
        applyStatusSelection(cards, radioButtons, selectedStatus[0]);

        new MaterialAlertDialogBuilder(this)
                .setTitle("修改订单状态")
                .setView(content)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    updateBatch(order, new OrderUpdateRequest(selectedStatus[0], null, null, null));
                })
                .show();
    }

    private MaterialCardView createStatusCard(int status,
                                              int[] selectedStatus,
                                              List<MaterialCardView> cards,
                                              List<RadioButton> radioButtons) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = dp(8);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(8));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setClickable(true);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        RadioButton radioButton = new RadioButton(this);
        radioButton.setClickable(false);
        radioButton.setFocusable(false);
        row.addView(radioButton);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(8);
        textColumn.setLayoutParams(textParams);

        TextView title = new TextView(this);
        title.setText(statusOptions[status]);
        title.setTextColor(getColor(R.color.cat_text));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        textColumn.addView(title);

        TextView description = new TextView(this);
        description.setText(statusDescriptions[status]);
        description.setTextColor(getColor(R.color.cat_muted));
        description.setTextSize(12);
        textColumn.addView(description);

        row.addView(textColumn);
        card.addView(row);

        card.setOnClickListener(v -> {
            selectedStatus[0] = status;
            applyStatusSelection(cards, radioButtons, status);
        });

        cards.add(card);
        radioButtons.add(radioButton);
        return card;
    }

    private void applyStatusSelection(List<MaterialCardView> cards, List<RadioButton> radioButtons, int selectedStatus) {
        for (int i = 0; i < cards.size(); i++) {
            boolean selected = i == selectedStatus;
            cards.get(i).setCardBackgroundColor(getColor(selected ? R.color.cat_surface_alt : R.color.white));
            cards.get(i).setStrokeColor(getColor(selected ? R.color.cat_primary : R.color.cat_border));
            radioButtons.get(i).setChecked(selected);
        }
    }

    private void confirmDelete(BatchOrderResponse order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除订单")
                .setMessage("确定删除该订单吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteBatch(order))
                .show();
    }

    private void updateBatch(BatchOrderResponse order, OrderUpdateRequest request) {
        List<Long> orderIds = orderIdsOf(order);
        if (orderIds.isEmpty()) {
            toast("订单明细为空");
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).updateOrder(orderIds.get(0), request), new ApiCallback<BatchOrderResponse>() {
            @Override
            public void onSuccess(BatchOrderResponse data) {
                toast("订单状态已更新为「" + UiText.orderStatus(request.orderStatus) + "」");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
                loadData();
            }
        });
    }

    private void deleteBatch(BatchOrderResponse order) {
        List<Long> orderIds = orderIdsOf(order);
        if (orderIds.isEmpty()) {
            toast("订单明细为空");
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).deleteOrder(orderIds.get(0)), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                toast("订单已删除");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
                loadData();
            }
        });
    }

    private List<Long> orderIdsOf(BatchOrderResponse order) {
        List<Long> ids = new ArrayList<>();
        if (order.items == null) {
            return ids;
        }
        for (OrderDetailItem item : order.items) {
            ids.add(item.orderId);
        }
        return ids;
    }

    private String productSummary(BatchOrderResponse order) {
        if (order.items == null || order.items.isEmpty()) {
            return "暂无商品明细";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < order.items.size(); i++) {
            OrderDetailItem item = order.items.get(i);
            if (i > 0) {
                builder.append("、");
            }
            builder.append(item.productName).append(" x").append(item.productQuantity);
        }
        return builder.toString();
    }

    private String detailText(BatchOrderResponse order) {
        StringBuilder builder = new StringBuilder();
        builder.append("状态：").append(UiText.orderStatus(order.orderStatus)).append("\n");
        builder.append("联系人：").append(order.userName).append(" · ").append(order.userPhone).append("\n");
        builder.append("总计：").append(UiText.price(order.totalAmount)).append("\n");
        if (order.orderNote != null && !order.orderNote.trim().isEmpty()) {
            builder.append("备注：").append(order.orderNote).append("\n");
        }
        builder.append("\n商品明细：");
        if (order.items == null || order.items.isEmpty()) {
            builder.append("\n暂无商品明细");
        } else {
            for (OrderDetailItem item : order.items) {
                builder.append("\n#")
                        .append(item.orderId)
                        .append(" ")
                        .append(item.productName)
                        .append(" x")
                        .append(item.productQuantity)
                        .append("  ")
                        .append(UiText.price(item.totalAmount));
            }
        }
        return builder.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
