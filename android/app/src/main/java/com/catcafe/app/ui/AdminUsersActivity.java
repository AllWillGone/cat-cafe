package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.AdminUserListItem;
import com.catcafe.app.model.AdminUserUpdateRequest;
import com.catcafe.app.model.PaginatedUsers;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class AdminUsersActivity extends AdminListActivityBase {
    private final String[] filters = {"全部用户", "普通用户", "管理员"};
    private AdminManageAdapter<AdminUserListItem> adapter;

    public static Intent intent(Context context) {
        return new Intent(context, AdminUsersActivity.class);
    }

    @Override
    protected void configure() {
        setTitleText("用户管理");
        keywordLayout.setHint("用户名或手机号搜索");
        setFilterLabels(filters);
        adapter = new AdminManageAdapter<>(this::bindUser);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        Integer userType = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).adminGetUsers(keyword(), userType, "userId", "desc", skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedUsers>() {
                    @Override
                    public void onSuccess(PaginatedUsers data) {
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

    private void bindUser(AdminManageAdapter.VH holder, AdminUserListItem user) {
        holder.image.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(AppConfig.buildImageUrl(user.userAvatar))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .circleCrop()
                .into(holder.image);
        holder.badge.setVisibility(View.VISIBLE);
        holder.badge.setText(user.userType == 1 ? "\u7ba1\u7406\u5458" : "\u666e\u901a\u7528\u6237");
        holder.title.setText(user.userName);
        holder.meta.setText("#" + user.userId + " · " + (user.userType == 1 ? "管理员" : "普通用户"));
        holder.body.setText("手机号：" + empty(user.userPhone, "未设置") + " · 性别：" + genderText(user.gender));
        holder.footer.setText("注册时间：" + empty(user.registerTime, "未知"));

        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("编辑");
        holder.primaryButton.setOnClickListener(v -> showForm(user));

        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setEnabled(user.userId != sessionManager.getUserId());
        holder.dangerButton.setOnClickListener(v -> confirmDelete(user));
    }

    private void showForm(AdminUserListItem user) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        form.setPadding(padding, 0, padding, 0);

        TextInputEditText nameInput = addInput(form, "用户名", user.userName, false);
        TextInputEditText phoneInput = addInput(form, "手机号", user.userPhone, false);
        TextInputEditText avatarInput = addInput(form, "头像 URL 或相对路径", user.userAvatar, false);
        Spinner genderInput = addSpinner(form, new String[]{"不修改", "男", "女"}, user.gender == null ? 0 : user.gender);
        TextInputEditText birthdayInput = addInput(form, "生日 yyyy-MM-dd，可留空不修改", "", false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("编辑用户")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    Integer gender = genderInput.getSelectedItemPosition() == 0 ? null : genderInput.getSelectedItemPosition();
                    String birthday = emptyToNull(textOf(birthdayInput));
                    if (!BirthdayRules.isBlankOrValid(birthday)) {
                        toast(BirthdayRules.ERROR_MESSAGE);
                        return;
                    }
                    AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                            textOf(nameInput), gender, birthday, textOf(phoneInput), textOf(avatarInput));
                    updateUser(user.userId, request, dialog);
                });
    }

    private void updateUser(long userId, AdminUserUpdateRequest request, AlertDialog dialog) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminUpdateUser(userId, request), new ApiCallback<AdminUserListItem>() {
            @Override
            public void onSuccess(AdminUserListItem data) {
                dialog.dismiss();
                toast("用户信息已更新");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void confirmDelete(AdminUserListItem user) {
        if (user.userId == sessionManager.getUserId()) {
            toast("不能删除自己的账号");
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除用户")
                .setMessage("有关联订单的用户会由后端拒绝删除。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteUser(user.userId))
                .show();
    }

    private void deleteUser(long userId) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminDeleteUser(userId), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                toast("用户已删除");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private TextInputEditText addInput(LinearLayout form, String hint, String value, boolean multiLine) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(hint);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextInputEditText input = new TextInputEditText(this);
        input.setText(value == null ? "" : value);
        input.setTextColor(getColor(R.color.cat_text));
        input.setSingleLine(!multiLine);
        layout.addView(input);
        form.addView(layout);
        return input;
    }

    private Spinner addSpinner(LinearLayout form, String[] options, int selected) {
        TextView title = new TextView(this);
        title.setText("鎬у埆");
        title.setTextColor(getColor(R.color.cat_muted));
        title.setTextSize(13);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(10);
        title.setLayoutParams(titleParams);
        form.addView(title);

        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));
        spinner.setSelection(Math.max(0, Math.min(selected, options.length - 1)));
        spinner.setVisibility(View.GONE);
        form.addView(spinner);

        ChipGroup chips = new ChipGroup(this);
        chips.setSingleSelection(true);
        chips.setSelectionRequired(true);
        form.addView(chips);
        int selectedIndex = spinner.getSelectedItemPosition();
        for (int i = 0; i < options.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(options[i]);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setTextColor(getColor(R.color.cat_text));
            chip.setChipStrokeWidth(dp(1));
            chip.setChipStrokeColorResource(i == selectedIndex ? R.color.cat_primary : R.color.cat_border);
            chip.setChipBackgroundColorResource(i == selectedIndex ? R.color.cat_surface_alt : R.color.white);
            final int index = i;
            chip.setOnClickListener(v -> {
                spinner.setSelection(index);
                updateChoiceChips(chips, index);
            });
            chips.addView(chip);
            if (i == selectedIndex) {
                chip.setChecked(true);
            }
        }
        return spinner;
    }

    private void updateChoiceChips(ChipGroup chips, int selectedIndex) {
        for (int i = 0; i < chips.getChildCount(); i++) {
            View child = chips.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setChipStrokeColorResource(i == selectedIndex ? R.color.cat_primary : R.color.cat_border);
                chip.setChipBackgroundColorResource(i == selectedIndex ? R.color.cat_surface_alt : R.color.white);
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String empty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private String genderText(Integer gender) {
        if (gender == null) {
            return "未设置";
        }
        return gender == 1 ? "男" : "女";
    }
}
