package com.catcafe.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.model.BatchOrderResponse;
import com.catcafe.app.model.OrderDetailItem;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {
    private final List<BatchOrderResponse> items = new ArrayList<>();
    private final Listener listener;

    public interface Listener {
        void onDetail(BatchOrderResponse order);

        void onEdit(BatchOrderResponse order);

        void onDelete(BatchOrderResponse order);
    }

    public OrderAdapter(List<BatchOrderResponse> data, Listener listener) {
        this.listener = listener;
        if (data != null) {
            items.addAll(data);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        BatchOrderResponse order = items.get(position);
        holder.title.setText(UiText.batchLabel(order));
        holder.meta.setText(UiText.orderStatus(order.orderStatus) + " · " + UiText.paymentMethod(order.paymentMethod));
        holder.products.setText(productSummary(order));
        holder.contact.setText(contactSummary(order));
        holder.total.setText(UiText.price(order.totalAmount));

        holder.editButton.setVisibility(order.orderStatus == 0 ? View.VISIBLE : View.GONE);
        holder.deleteButton.setVisibility(order.orderStatus == 3 || order.orderStatus == 4 ? View.VISIBLE : View.GONE);
        holder.actions.setVisibility(
                holder.editButton.getVisibility() == View.VISIBLE || holder.deleteButton.getVisibility() == View.VISIBLE
                        ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetail(order);
            }
        });
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(order);
            }
        });
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
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

    private String contactSummary(BatchOrderResponse order) {
        String name = order.userName == null ? "" : order.userName;
        String phone = order.userPhone == null ? "" : order.userPhone;
        if (phone.isEmpty()) {
            return name;
        }
        if (name.isEmpty()) {
            return phone;
        }
        return name + " · " + phone;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        TextView products;
        TextView contact;
        TextView total;
        View actions;
        MaterialButton editButton;
        MaterialButton deleteButton;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.orderTitle);
            meta = itemView.findViewById(R.id.orderMeta);
            products = itemView.findViewById(R.id.orderProducts);
            contact = itemView.findViewById(R.id.orderContact);
            total = itemView.findViewById(R.id.orderTotal);
            actions = itemView.findViewById(R.id.orderActions);
            editButton = itemView.findViewById(R.id.orderEditButton);
            deleteButton = itemView.findViewById(R.id.orderDeleteButton);
        }
    }
}
