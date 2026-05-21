package com.catcafe.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.catcafe.app.core.CartItem;
import com.catcafe.app.core.CartManager;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    private final List<CartItem> items = new ArrayList<>();
    private final Runnable onChanged;

    public CartAdapter(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public void submit(List<CartItem> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem item = items.get(position);
        holder.name.setText(item.product.productName);
        holder.meta.setText(String.valueOf(item.quantity));
        holder.minus.setOnClickListener(v -> {
            CartManager cart = CartManager.getInstance();
            cart.setQuantity(item.product.productId, cart.getQuantity(item.product.productId) - 1);
            submit(cart.getItems());
            onChanged.run();
        });
        holder.plus.setOnClickListener(v -> {
            CartManager cart = CartManager.getInstance();
            cart.setQuantity(item.product.productId, cart.getQuantity(item.product.productId) + 1);
            submit(cart.getItems());
            onChanged.run();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        TextView meta;
        ImageButton minus;
        ImageButton plus;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cartItemName);
            meta = itemView.findViewById(R.id.cartItemMeta);
            minus = itemView.findViewById(R.id.cartItemMinus);
            plus = itemView.findViewById(R.id.cartItemPlus);
        }
    }
}
