package com.catcafe.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class AdminManageAdapter<T> extends RecyclerView.Adapter<AdminManageAdapter.VH> {
    public interface Binder<T> {
        void bind(VH holder, T item);
    }

    private final List<T> items = new ArrayList<>();
    private final Binder<T> binder;

    public AdminManageAdapter(Binder<T> binder) {
        this.binder = binder;
    }

    public void submitList(List<T> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_manage, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.reset();
        binder.bind(holder, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        public final TextView title;
        public final ShapeableImageView image;
        public final TextView badge;
        public final TextView meta;
        public final TextView body;
        public final TextView footer;
        public final View actions;
        public final MaterialButton primaryButton;
        public final MaterialButton secondaryButton;
        public final MaterialButton dangerButton;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.adminItemTitle);
            image = itemView.findViewById(R.id.adminItemImage);
            badge = itemView.findViewById(R.id.adminItemBadge);
            meta = itemView.findViewById(R.id.adminItemMeta);
            body = itemView.findViewById(R.id.adminItemBody);
            footer = itemView.findViewById(R.id.adminItemFooter);
            actions = itemView.findViewById(R.id.adminItemActions);
            primaryButton = itemView.findViewById(R.id.adminPrimaryButton);
            secondaryButton = itemView.findViewById(R.id.adminSecondaryButton);
            dangerButton = itemView.findViewById(R.id.adminDangerButton);
        }

        void reset() {
            title.setText("");
            image.setVisibility(View.GONE);
            image.setImageResource(R.drawable.ic_image_placeholder);
            badge.setText("");
            badge.setVisibility(View.GONE);
            meta.setText("");
            body.setText("");
            footer.setText("");
            primaryButton.setVisibility(View.GONE);
            secondaryButton.setVisibility(View.GONE);
            dangerButton.setVisibility(View.GONE);
            primaryButton.setOnClickListener(null);
            secondaryButton.setOnClickListener(null);
            dangerButton.setOnClickListener(null);
            actions.setVisibility(View.VISIBLE);
        }
    }
}
