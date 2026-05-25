package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.CatDetail;
import com.catcafe.app.model.CatWriteRequest;
import com.catcafe.app.model.PaginatedCats;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class AdminCatsActivity extends AdminListActivityBase {
    private final String[] filters = {"全部猫咪", "休息中", "在岗中"};
    private AdminManageAdapter<CatDetail> adapter;
    private ActivityResultLauncher<String> pickCatImage;
    private TextInputEditText activePhotoInput;
    private ImageView activePhotoPreview;

    public static Intent intent(Context context) {
        return new Intent(context, AdminCatsActivity.class);
    }

    @Override
    protected void configure() {
        pickCatImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && activePhotoInput != null && activePhotoPreview != null) {
                        AdminImageUploadHelper.upload(this, uri, "cat", "cat", activePhotoInput, activePhotoPreview);
                    }
                }
        );
        setTitleText("猫咪管理");
        keywordLayout.setHint("猫名、品种、性格搜索");
        setFilterLabels(filters);
        addButton.setVisibility(View.VISIBLE);
        addButton.setText("新增猫咪");
        addButton.setOnClickListener(v -> showForm(null));
        adapter = new AdminManageAdapter<>(this::bindCat);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        Integer status = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).adminGetCats(status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedCats>() {
                    @Override
                    public void onSuccess(PaginatedCats data) {
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

    private void bindCat(AdminManageAdapter.VH holder, CatDetail cat) {
        holder.image.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(AppConfig.buildImageUrl(cat.photoUrl))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.image);
        holder.badge.setVisibility(View.VISIBLE);
        holder.badge.setText(UiText.catStatus(cat.status));
        holder.title.setText(cat.catName);
        holder.meta.setText("#" + cat.catId + " · " + cat.breed + " · " + UiText.catStatus(cat.status));
        holder.body.setText("生日 " + cat.birthday + "\n" + cat.personality);
        holder.footer.setText(cat.notes == null || cat.notes.trim().isEmpty() ? "暂无备注" : cat.notes);

        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("编辑");
        holder.primaryButton.setOnClickListener(v -> showForm(cat));

        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText(cat.status == 1 ? "休息" : "在岗");
        holder.secondaryButton.setOnClickListener(v -> updateCat(cat.catId,
                new CatWriteRequest(null, null, null, cat.status == 1 ? 0 : 1, null, null, null),
                "状态已更新"));

        holder.secondaryButton.setOnClickListener(v -> showStatusDialog(cat));
        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDelete(cat));
    }

    private void showStatusDialog(CatDetail cat) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        content.setPadding(padding, 0, padding, 0);

        TextView summary = new TextView(this);
        summary.setText(cat.catName + "\n" + cat.breed);
        summary.setTextColor(getColor(R.color.cat_muted));
        summary.setTextSize(13);
        content.addView(summary);

        final int[] selectedStatus = {Math.max(0, Math.min(cat.status, 1))};
        ChipGroup chips = new ChipGroup(this);
        chips.setSingleSelection(true);
        chips.setSelectionRequired(true);
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipParams.topMargin = dp(10);
        chips.setLayoutParams(chipParams);
        content.addView(chips);
        addStatusChip(chips, "\u4f11\u606f\u4e2d", 0, selectedStatus);
        addStatusChip(chips, "\u5728\u5c97\u4e2d", 1, selectedStatus);
        updateChoiceChips(chips, selectedStatus[0]);

        new MaterialAlertDialogBuilder(this)
                .setTitle("\u4fee\u6539\u732b\u54aa\u72b6\u6001")
                .setView(content)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u4fdd\u5b58", (dialog, which) -> {
                    if (selectedStatus[0] == cat.status) {
                        return;
                    }
                    updateCat(cat.catId,
                            new CatWriteRequest(null, null, null, selectedStatus[0], null, null, null),
                            "\u72b6\u6001\u5df2\u66f4\u65b0");
                })
                .show();
    }

    private void addStatusChip(ChipGroup chips, String label, int status, int[] selectedStatus) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setTextColor(getColor(R.color.cat_text));
        chip.setChipStrokeWidth(dp(1));
        chip.setOnClickListener(v -> {
            selectedStatus[0] = status;
            updateChoiceChips(chips, status);
        });
        chips.addView(chip);
        if (status == selectedStatus[0]) {
            chip.setChecked(true);
        }
    }

    private void showForm(CatDetail cat) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        form.setPadding(padding, 0, padding, 0);

        TextInputEditText nameInput = addInput(form, "名字", cat == null ? "" : cat.catName, false);
        TextInputEditText breedInput = addInput(form, "品种", cat == null ? "" : cat.breed, false);
        TextInputEditText birthdayInput = addInput(form, "生日 yyyy-MM-dd", cat == null ? "" : cat.birthday, false);
        Spinner statusInput = addSpinner(form, new String[]{"休息中", "在岗中"}, cat == null ? 1 : cat.status);
        TextInputEditText personalityInput = addInput(form, "性格", cat == null ? "" : cat.personality, true);
        TextInputEditText photoInput = addInput(form, "照片 URL 或相对路径", cat == null ? "" : cat.photoUrl, false);
        ImageView photoPreview = addImagePreview(form, cat == null ? "" : cat.photoUrl);
        addUploadButton(form, "上传猫咪照片", photoInput, photoPreview);
        TextInputEditText notesInput = addInput(form, "备注", cat == null ? "" : cat.notes, true);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(cat == null ? "新增猫咪" : "编辑猫咪")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    CatWriteRequest request = catRequest(nameInput, breedInput, birthdayInput, statusInput, personalityInput, photoInput, notesInput);
                    if (request == null) {
                        return;
                    }
                    if (cat == null) {
                        createCat(request, dialog);
                    } else {
                        updateCat(cat.catId, request, "猫咪信息已更新", dialog);
                    }
                });
    }

    private CatWriteRequest catRequest(TextInputEditText nameInput, TextInputEditText breedInput, TextInputEditText birthdayInput,
                                       Spinner statusInput, TextInputEditText personalityInput, TextInputEditText photoInput,
                                       TextInputEditText notesInput) {
        String name = textOf(nameInput);
        String breed = textOf(breedInput);
        String birthday = textOf(birthdayInput);
        String personality = textOf(personalityInput);
        String photo = textOf(photoInput);
        String notes = textOf(notesInput);
        if (name.isEmpty() || breed.isEmpty() || birthday.isEmpty() || personality.isEmpty() || photo.isEmpty() || notes.isEmpty()) {
            toast("请填写猫咪完整信息");
            return null;
        }
        if (!BirthdayRules.isValid(birthday)) {
            toast(BirthdayRules.ERROR_MESSAGE);
            return null;
        }
        return new CatWriteRequest(name, breed, birthday, statusInput.getSelectedItemPosition(), personality, photo, notes);
    }

    private void createCat(CatWriteRequest request, AlertDialog dialog) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminCreateCat(request), new ApiCallback<CatDetail>() {
            @Override
            public void onSuccess(CatDetail data) {
                dialog.dismiss();
                toast("猫咪已新增");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void updateCat(long catId, CatWriteRequest request, String successMessage) {
        updateCat(catId, request, successMessage, null);
    }

    private void updateCat(long catId, CatWriteRequest request, String successMessage, AlertDialog dialog) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminUpdateCat(catId, request), new ApiCallback<CatDetail>() {
            @Override
            public void onSuccess(CatDetail data) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                toast(successMessage);
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void confirmDelete(CatDetail cat) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除猫咪")
                .setMessage("删除不会自动删除历史评论。确认删除这只猫咪？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteCat(cat.catId))
                .show();
    }

    private void deleteCat(long catId) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminDeleteCat(catId), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                toast("猫咪信息已删除");
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
        if (multiLine) {
            input.setMinLines(2);
            input.setMaxLines(4);
            input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        }
        layout.addView(input);
        form.addView(layout);
        return input;
    }

    private ImageView addImagePreview(LinearLayout form, String path) {
        ImageView preview = new ImageView(this);
        int size = (int) (96 * getResources().getDisplayMetrics().density);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.topMargin = margin;
        params.bottomMargin = margin;
        preview.setLayoutParams(params);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        form.addView(preview);
        AdminImageUploadHelper.loadPreview(this, preview, path);
        return preview;
    }

    private void addUploadButton(LinearLayout form, String label, TextInputEditText input, ImageView preview) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setOnClickListener(v -> {
            activePhotoInput = input;
            activePhotoPreview = preview;
            pickCatImage.launch("image/*");
        });
        form.addView(button);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AdminImageUploadHelper.loadPreview(AdminCatsActivity.this, preview, s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private Spinner addSpinner(LinearLayout form, String[] options, int selected) {
        TextView title = new TextView(this);
        title.setText("鐘舵€?");
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
}
