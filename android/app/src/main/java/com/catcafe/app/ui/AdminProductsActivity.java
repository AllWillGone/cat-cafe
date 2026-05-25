package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.PaginatedProducts;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.model.ProductWriteRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminProductsActivity extends AdminListActivityBase {
    private final String[] filters = {"全部商品", "服务", "餐饮", "猫咖用品", "在售", "已下架"};
    private AdminManageAdapter<ProductDetail> adapter;
    private ActivityResultLauncher<String> pickProductImage;
    private TextInputEditText activeImageInput;
    private ImageView activeImagePreview;

    public static Intent intent(Context context) {
        return new Intent(context, AdminProductsActivity.class);
    }

    @Override
    protected void configure() {
        pickProductImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && activeImageInput != null && activeImagePreview != null) {
                        AdminImageUploadHelper.upload(this, uri, "product", "product", activeImageInput, activeImagePreview);
                    }
                }
        );
        setTitleText("商品管理");
        keywordLayout.setHint("商品名搜索");
        setFilterLabels(filters);
        addButton.setVisibility(View.VISIBLE);
        addButton.setText("新增商品");
        addButton.setOnClickListener(v -> showForm(null));
        adapter = new AdminManageAdapter<>(this::bindProduct);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void loadData() {
        showLoading(true);
        Integer category = null;
        Integer status = null;
        int selected = filterSpinner.getSelectedItemPosition();
        if (selected >= 1 && selected <= 3) {
            category = selected - 1;
        } else if (selected == 4) {
            status = 1;
        } else if (selected == 5) {
            status = 0;
        }
        NetworkHelper.enqueue(this,
                ApiClient.getService(this).adminGetProducts(category, status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedProducts>() {
                    @Override
                    public void onSuccess(PaginatedProducts data) {
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

    private void bindProduct(AdminManageAdapter.VH holder, ProductDetail product) {
        holder.image.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(AppConfig.buildImageUrl(product.imageUrl))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.image);
        holder.badge.setVisibility(View.VISIBLE);
        holder.badge.setText(UiText.productStatus(product.status));
        holder.title.setText(product.productName);
        holder.meta.setText("#" + product.productId + " · " + UiText.productCategory(product.category) + " · " + UiText.productStatus(product.status));
        holder.body.setText(UiText.productPrice(product) + " · 库存 " + product.stockQuantity);
        holder.footer.setText(product.description == null || product.description.trim().isEmpty() ? "暂无描述" : product.description);

        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("编辑");
        holder.primaryButton.setOnClickListener(v -> showForm(product));

        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText(product.status == 1 ? "下架" : "上架");
        holder.secondaryButton.setOnClickListener(v -> {
            if (product.status == 1) {
                delistProduct(product);
            } else {
                updateProduct(product.productId, new ProductWriteRequest(null, null, null, null, null, null, 1), "商品已上架");
            }
        });

        holder.secondaryButton.setOnClickListener(v -> showStatusDialog(product));
        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDelete(product));
    }

    private void showStatusDialog(ProductDetail product) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        content.setPadding(padding, 0, padding, 0);

        TextView summary = new TextView(this);
        summary.setText(product.productName + "\n" + UiText.productPrice(product));
        summary.setTextColor(getColor(R.color.cat_muted));
        summary.setTextSize(13);
        content.addView(summary);

        final int[] selectedStatus = {Math.max(0, Math.min(product.status, 1))};
        ChipGroup chips = new ChipGroup(this);
        chips.setSingleSelection(true);
        chips.setSelectionRequired(true);
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipParams.topMargin = dp(10);
        chips.setLayoutParams(chipParams);
        content.addView(chips);
        addStatusChip(chips, "\u5df2\u4e0b\u67b6", 0, selectedStatus);
        addStatusChip(chips, "\u5728\u552e", 1, selectedStatus);
        updateChoiceChips(chips, selectedStatus[0]);

        new MaterialAlertDialogBuilder(this)
                .setTitle("\u4fee\u6539\u5546\u54c1\u72b6\u6001")
                .setView(content)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u4fdd\u5b58", (dialog, which) -> {
                    if (selectedStatus[0] == product.status) {
                        return;
                    }
                    if (selectedStatus[0] == 0) {
                        delistProduct(product);
                    } else {
                        updateProduct(product.productId, new ProductWriteRequest(null, null, null, null, null, null, 1), "\u5546\u54c1\u5df2\u4e0a\u67b6");
                    }
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

    private void showForm(ProductDetail product) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        form.setPadding(padding, 0, padding, 0);
        TextView formMarker = new TextView(this);
        formMarker.setText("\u5f53\u524d\u7248\u672c\uff1a\u5361\u7247\u5f0f\u5546\u54c1\u7f16\u8f91");
        formMarker.setTextColor(getColor(R.color.cat_primary_dark));
        formMarker.setTextSize(13);
        formMarker.setTypeface(null, Typeface.BOLD);
        form.addView(formMarker);
        TextInputEditText nameInput = addInput(form, "名称", product == null ? "" : product.productName, false);
        Spinner categoryInput = addSpinner(form, "分类", new String[]{"服务", "餐饮", "猫咖用品"}, product == null ? 0 : product.category);
        TextInputEditText priceInput = addInput(form, "价格", product == null || product.price == null ? "" : product.price.toPlainString(), false);
        TextInputEditText stockInput = addInput(form, "库存", product == null ? "" : String.valueOf(product.stockQuantity), false);
        TextInputEditText imageInput = addInput(form, "图片 URL 或相对路径", product == null ? "" : product.imageUrl, false);
        ImageView imagePreview = addImagePreview(form, product == null ? "" : product.imageUrl);
        addUploadButton(form, "上传商品图片", imageInput, imagePreview);
        TextInputEditText descInput = addInput(form, "描述", product == null ? "" : product.description, true);
        Spinner statusInput = addSpinner(form, "状态", new String[]{"已下架", "在售"}, product == null ? 1 : product.status);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(product == null ? "新增商品" : "编辑商品")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    ProductWriteRequest request = productRequest(nameInput, categoryInput, priceInput, stockInput, imageInput, descInput, statusInput);
                    if (request == null) {
                        return;
                    }
                    if (product == null) {
                        createProduct(request, dialog);
                    } else {
                        updateProduct(product.productId, request, "商品已更新", dialog);
                    }
                });
    }

    private ProductWriteRequest productRequest(TextInputEditText nameInput, Spinner categoryInput, TextInputEditText priceInput,
                                               TextInputEditText stockInput, TextInputEditText imageInput,
                                               TextInputEditText descInput, Spinner statusInput) {
        String name = textOf(nameInput);
        String priceText = textOf(priceInput);
        String stockText = textOf(stockInput);
        String image = textOf(imageInput);
        if (name.isEmpty() || priceText.isEmpty() || stockText.isEmpty() || image.isEmpty()) {
            toast("请填写名称、价格、库存和图片路径");
            return null;
        }
        try {
            return new ProductWriteRequest(
                    name,
                    categoryInput.getSelectedItemPosition(),
                    new BigDecimal(priceText),
                    Integer.parseInt(stockText),
                    image,
                    textOf(descInput),
                    statusInput.getSelectedItemPosition()
            );
        } catch (NumberFormatException ex) {
            toast("价格或库存格式不正确");
            return null;
        }
    }

    private void createProduct(ProductWriteRequest request, AlertDialog dialog) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminCreateProduct(request), new ApiCallback<ProductDetail>() {
            @Override
            public void onSuccess(ProductDetail data) {
                dialog.dismiss();
                toast("商品已新增");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void updateProduct(long productId, ProductWriteRequest request, String successMessage) {
        updateProduct(productId, request, successMessage, null);
    }

    private void updateProduct(long productId, ProductWriteRequest request, String successMessage, AlertDialog dialog) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminUpdateProduct(productId, request), new ApiCallback<ProductDetail>() {
            @Override
            public void onSuccess(ProductDetail data) {
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

    private void delistProduct(ProductDetail product) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminDelistProduct(product.productId), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                toast("商品已下架");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void confirmDelete(ProductDetail product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除商品")
                .setMessage("有历史订单时后端会改为下架保留。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteProduct(product.productId))
                .show();
    }

    private void deleteProduct(long productId) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminDeleteProduct(productId), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                Object message = data == null ? null : data.get("message");
                toast(message == null ? "操作已完成" : String.valueOf(message));
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
            activeImageInput = input;
            activeImagePreview = preview;
            pickProductImage.launch("image/*");
        });
        form.addView(button);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                AdminImageUploadHelper.loadPreview(AdminProductsActivity.this, preview, s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private Spinner addSpinner(LinearLayout form, String label, String[] options, int selected) {
        TextView title = new TextView(this);
        title.setText(label);
        title.setTextColor(getColor(R.color.cat_muted));
        title.setTextSize(13);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(10);
        title.setLayoutParams(titleParams);
        form.addView(title);

        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));
        spinner.setSelection(Math.max(0, Math.min(selected, options.length - 1)));
        spinner.setPrompt(label);
        spinner.setVisibility(View.GONE);
        form.addView(spinner);

        int selectedIndex = spinner.getSelectedItemPosition();
        List<MaterialCardView> cards = new ArrayList<>();
        List<RadioButton> radioButtons = new ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            form.addView(createChoiceCard(options[i], index, spinner, cards, radioButtons));
        }
        applyChoiceSelection(cards, radioButtons, selectedIndex);
        return spinner;
    }

    private MaterialCardView createChoiceCard(String label,
                                              int index,
                                              Spinner spinner,
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

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(getColor(R.color.cat_text));
        text.setTextSize(15);
        text.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(8);
        text.setLayoutParams(textParams);
        row.addView(text);

        card.addView(row);
        card.setOnClickListener(v -> {
            spinner.setSelection(index);
            applyChoiceSelection(cards, radioButtons, index);
        });

        cards.add(card);
        radioButtons.add(radioButton);
        return card;
    }

    private void applyChoiceSelection(List<MaterialCardView> cards, List<RadioButton> radioButtons, int selectedIndex) {
        for (int i = 0; i < cards.size(); i++) {
            boolean selected = i == selectedIndex;
            cards.get(i).setCardBackgroundColor(getColor(selected ? R.color.cat_surface_alt : R.color.white));
            cards.get(i).setStrokeColor(getColor(selected ? R.color.cat_primary : R.color.cat_border));
            radioButtons.get(i).setChecked(selected);
        }
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
