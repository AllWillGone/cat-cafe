package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminOrdersActivity extends AdminListActivityBase {
    private final String[] filters = {"全部订单", "未支付", "已支付", "待取货", "已完成", "已取消"};
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
        holder.secondaryButton.setText("编辑");
        holder.secondaryButton.setOnClickListener(v -> showEdit(order));

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

    private void showEdit(BatchOrderResponse order) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        form.setPadding(padding, 0, padding, 0);

        Spinner statusInput = addSpinner(form, filters, order.orderStatus + 1);
        TextInputEditText nameInput = addInput(form, "联系人", order.userName, false);
        TextInputEditText phoneInput = addInput(form, "手机号", order.userPhone, false);
        TextInputEditText noteInput = addInput(form, "备注", order.orderNote, true);

        new MaterialAlertDialogBuilder(this)
                .setTitle("编辑订单")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String userName = textOf(nameInput);
                    String userPhone = textOf(phoneInput);
                    if (userName.isEmpty() || userPhone.isEmpty()) {
                        toast("请填写联系人和手机号");
                        return;
                    }
                    int selected = statusInput.getSelectedItemPosition();
                    Integer status = selected == 0 ? null : selected - 1;
                    if (status != null && status == 4 && isMultiItemBatch(order)) {
                        toast("多商品批次取消会重复恢复库存，需要先确认后端批次逻辑");
                        return;
                    }
                    updateBatch(order, new OrderUpdateRequest(status, userPhone, userName, textOf(noteInput)));
                })
                .show();
    }

    private void confirmDelete(BatchOrderResponse order) {
        if (isMultiItemBatch(order) && order.orderStatus != 3 && order.orderStatus != 4) {
            toast("多商品非终态批次删除会重复恢复库存，需要先确认后端批次逻辑");
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除订单")
                .setMessage("将逐条删除该批次内的所有订单明细。非终态订单删除时由后端恢复库存。")
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
        updateNext(orderIds, 0, request);
    }

    private void updateNext(List<Long> orderIds, int index, OrderUpdateRequest request) {
        if (index >= orderIds.size()) {
            toast("订单已更新");
            loadData();
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).updateOrder(orderIds.get(index), request), new ApiCallback<BatchOrderResponse>() {
            @Override
            public void onSuccess(BatchOrderResponse data) {
                updateNext(orderIds, index + 1, request);
            }

            @Override
            public void onError(String message) {
                toast("部分订单未更新：" + message);
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
        deleteNext(orderIds, 0);
    }

    private void deleteNext(List<Long> orderIds, int index) {
        if (index >= orderIds.size()) {
            toast("订单已删除");
            loadData();
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).deleteOrder(orderIds.get(index)), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                deleteNext(orderIds, index + 1);
            }

            @Override
            public void onError(String message) {
                toast("部分订单未删除：" + message);
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

    private boolean isMultiItemBatch(BatchOrderResponse order) {
        return order.items != null && order.items.size() > 1;
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

    private TextInputEditText addInput(LinearLayout form, String hint, String value, boolean multiLine) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(hint);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextInputEditText input = new TextInputEditText(this);
        input.setText(value == null ? "" : value);
        input.setSingleLine(!multiLine);
        if (multiLine) {
            input.setMinLines(2);
            input.setMaxLines(3);
        }
        layout.addView(input);
        form.addView(layout);
        return input;
    }

    private Spinner addSpinner(LinearLayout form, String[] options, int selected) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));
        spinner.setSelection(Math.max(0, Math.min(selected, options.length - 1)));
        form.addView(spinner);
        return spinner;
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
