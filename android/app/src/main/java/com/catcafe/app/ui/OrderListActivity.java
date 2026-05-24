package com.catcafe.app.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.model.BatchOrderResponse;
import com.catcafe.app.model.OrderDetailItem;
import com.catcafe.app.model.OrderUpdateRequest;
import com.catcafe.app.model.PaginatedOrders;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderListActivity extends BaseToolbarActivity implements OrderAdapter.Listener {
    private final String[] statusLabels = {"全部状态", "未支付", "已支付", "待取货", "已完成", "已取消"};
    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;
    private Spinner statusFilter;
    private TextInputEditText keywordInput;

    public static Intent intent(Context context) {
        return new Intent(context, OrderListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        setupToolbar(R.id.orderToolbar);

        list = findViewById(R.id.orderList);
        refreshLayout = findViewById(R.id.orderRefresh);
        statusFilter = findViewById(R.id.orderStatusFilter);
        keywordInput = findViewById(R.id.orderKeywordInput);
        MaterialButton searchButton = findViewById(R.id.orderSearchButton);

        list.setLayoutManager(new LinearLayoutManager(this));
        refreshLayout.setOnRefreshListener(this::loadOrders);
        statusFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusLabels));
        searchButton.setOnClickListener(v -> loadOrders());
        loadOrders();
    }

    @Override
    public void onDetail(BatchOrderResponse order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(UiText.batchLabel(order))
                .setMessage(orderDetailText(order))
                .setPositiveButton("知道了", null)
                .show();
    }

    @Override
    public void onPay(BatchOrderResponse order) {
        if (order.orderStatus != 0) {
            Toast.makeText(this, "只能支付未支付订单", Toast.LENGTH_SHORT).show();
            return;
        }

        String batchNo = order.batchNo == null ? "N/A" : order.batchNo;
        String amount = order.totalAmount == null ? "0" : order.totalAmount.toPlainString();
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=PAY_"
                + batchNo + "_" + amount;

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        content.setPadding(padding, 0, padding, 0);

        ImageView qrCode = new ImageView(this);
        int size = Math.round(200 * getResources().getDisplayMetrics().density);
        qrCode.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        qrCode.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(qrCodeUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(qrCode);
        content.addView(qrCode);

        TextView batchText = new TextView(this);
        batchText.setGravity(Gravity.CENTER);
        batchText.setTextColor(getColor(R.color.cat_muted));
        batchText.setTextSize(13);
        batchText.setText("批次号: " + ellipsizeBatchNo(batchNo));
        content.addView(batchText);

        TextView amountText = new TextView(this);
        amountText.setGravity(Gravity.CENTER);
        amountText.setTextColor(getColor(R.color.cat_text));
        amountText.setTextSize(20);
        amountText.setTypeface(null, android.graphics.Typeface.BOLD);
        amountText.setText(UiText.price(order.totalAmount));
        content.addView(amountText);

        new MaterialAlertDialogBuilder(this)
                .setTitle("扫码支付")
                .setView(content)
                .setPositiveButton("完成", null)
                .show();
    }

    @Override
    public void onEdit(BatchOrderResponse order) {
        if (order.orderStatus != 0) {
            Toast.makeText(this, "只能修改未支付订单", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        form.setPadding(padding, 0, padding, 0);

        TextInputEditText nameInput = addInput(form, "联系人", order.userName, false);
        TextInputEditText phoneInput = addInput(form, "手机号", order.userPhone, false);
        TextInputEditText noteInput = addInput(form, "备注", order.orderNote, true);

        AlertDialog editDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("编辑订单")
                .setView(form)
                .setNegativeButton("关闭", null)
                .setNeutralButton("取消订单", null)
                .setPositiveButton("保存", null)
                .show();
        editDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getColor(R.color.like_red));
        editDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> confirmCancelOrder(order, editDialog));
        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String userName = textOf(nameInput);
            String userPhone = textOf(phoneInput);
            if (userName.isEmpty() || userPhone.isEmpty()) {
                Toast.makeText(this, "请填写联系人和手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            OrderUpdateRequest request = new OrderUpdateRequest(null, userPhone, userName, textOf(noteInput));
            updateBatch(order, request, editDialog);
        });
    }

    @Override
    public void onDelete(BatchOrderResponse order) {
        if (order.orderStatus != 3 && order.orderStatus != 4) {
            Toast.makeText(this, "只能删除已完成或已取消的订单", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除订单")
                .setMessage("将删除这笔订单下的所有商品记录。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteBatch(order))
                .show();
    }

    private void loadOrders() {
        Integer status = statusFilter.getSelectedItemPosition() == 0 ? null : statusFilter.getSelectedItemPosition() - 1;
        String keyword = textOf(keywordInput);
        if (keyword.isEmpty()) {
            keyword = null;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).getOrders(status, keyword, 0, 50), new ApiCallback<PaginatedOrders>() {
            @Override
            public void onSuccess(PaginatedOrders data) {
                refreshLayout.setRefreshing(false);
                list.setAdapter(new OrderAdapter(data.items, OrderListActivity.this));
            }

            @Override
            public void onError(String message) {
                refreshLayout.setRefreshing(false);
                Toast.makeText(OrderListActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String orderDetailText(BatchOrderResponse order) {
        StringBuilder builder = new StringBuilder();
        builder.append("状态：").append(UiText.orderStatus(order.orderStatus)).append("\n");
        builder.append("支付方式：").append(UiText.paymentMethod(order.paymentMethod)).append("\n");
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
                builder.append("\n")
                        .append(item.productName)
                        .append(" x")
                        .append(item.productQuantity)
                        .append("  ")
                        .append(UiText.price(item.totalAmount));
            }
        }
        return builder.toString();
    }

    private void updateBatch(BatchOrderResponse order, OrderUpdateRequest request) {
        updateBatch(order, request, null);
    }

    private void updateBatch(BatchOrderResponse order, OrderUpdateRequest request, AlertDialog dialog) {
        List<Long> orderIds = orderIdsOf(order);
        if (orderIds.isEmpty()) {
            Toast.makeText(this, "订单明细为空", Toast.LENGTH_SHORT).show();
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).updateOrder(orderIds.get(0), request), new ApiCallback<BatchOrderResponse>() {
            @Override
            public void onSuccess(BatchOrderResponse data) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Toast.makeText(OrderListActivity.this, "订单已更新", Toast.LENGTH_SHORT).show();
                loadOrders();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OrderListActivity.this, message, Toast.LENGTH_SHORT).show();
                loadOrders();
            }
        });
    }

    private void confirmCancelOrder(BatchOrderResponse order, AlertDialog editDialog) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("取消订单")
                .setMessage("取消后将恢复库存，订单不可继续编辑。")
                .setNegativeButton("再想想", null)
                .setPositiveButton("确认取消", (dialog, which) -> {
                    editDialog.dismiss();
                    cancelBatch(order);
                })
                .show();
    }

    private void cancelBatch(BatchOrderResponse order) {
        List<Long> orderIds = orderIdsOf(order);
        if (orderIds.isEmpty()) {
            Toast.makeText(this, "订单明细为空", Toast.LENGTH_SHORT).show();
            return;
        }
        OrderUpdateRequest request = new OrderUpdateRequest(4, null, null, null);
        NetworkHelper.enqueue(this, ApiClient.getService(this).updateOrder(orderIds.get(0), request), new ApiCallback<BatchOrderResponse>() {
            @Override
            public void onSuccess(BatchOrderResponse data) {
                Toast.makeText(OrderListActivity.this, "订单已取消", Toast.LENGTH_SHORT).show();
                loadOrders();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OrderListActivity.this, message, Toast.LENGTH_SHORT).show();
                loadOrders();
            }
        });
    }

    private void updateNext(List<Long> orderIds, int index, OrderUpdateRequest request) {
        if (index >= orderIds.size()) {
            Toast.makeText(this, "订单已更新", Toast.LENGTH_SHORT).show();
            loadOrders();
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).updateOrder(orderIds.get(index), request), new ApiCallback<BatchOrderResponse>() {
            @Override
            public void onSuccess(BatchOrderResponse data) {
                updateNext(orderIds, index + 1, request);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OrderListActivity.this, "部分订单未更新：" + message, Toast.LENGTH_SHORT).show();
                loadOrders();
            }
        });
    }

    private void deleteBatch(BatchOrderResponse order) {
        List<Long> orderIds = orderIdsOf(order);
        if (orderIds.isEmpty()) {
            Toast.makeText(this, "订单明细为空", Toast.LENGTH_SHORT).show();
            return;
        }
        NetworkHelper.enqueue(this, ApiClient.getService(this).deleteOrder(orderIds.get(0)), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                Toast.makeText(OrderListActivity.this, "订单已删除", Toast.LENGTH_SHORT).show();
                loadOrders();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OrderListActivity.this, message, Toast.LENGTH_SHORT).show();
                loadOrders();
            }
        });
    }

    private List<Long> orderIdsOf(BatchOrderResponse order) {
        List<Long> orderIds = new ArrayList<>();
        if (order.items == null) {
            return orderIds;
        }
        for (OrderDetailItem item : order.items) {
            orderIds.add(item.orderId);
        }
        return orderIds;
    }

    private String ellipsizeBatchNo(String batchNo) {
        if (batchNo == null || batchNo.length() <= 18) {
            return batchNo == null ? "" : batchNo;
        }
        return batchNo.substring(0, 18) + "...";
    }

    private TextInputEditText addInput(LinearLayout form, String hint, String value, boolean multiLine) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(hint);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextInputEditText input = new TextInputEditText(this);
        input.setText(value == null ? "" : value);
        input.setTextColor(getColor(R.color.cat_text));
        input.setSingleLine(!multiLine);
        if (multiLine) {
            input.setMinLines(2);
            input.setMaxLines(3);
            input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        }

        layout.addView(input);
        form.addView(layout);
        return input;
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
