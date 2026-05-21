package com.catcafe.app.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.model.PaginatedProducts;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.model.ProductWriteRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.Map;

public class AdminProductsActivity extends AdminListActivityBase {
    private final String[] filters = {"全部商品", "服务", "餐饮", "猫咖用品", "在售", "已下架"};
    private AdminManageAdapter<ProductDetail> adapter;

    public static Intent intent(Context context) {
        return new Intent(context, AdminProductsActivity.class);
    }

    @Override
    protected void configure() {
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

        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDelete(product));
    }

    private void showForm(ProductDetail product) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
        form.setPadding(padding, 0, padding, 0);
        TextInputEditText nameInput = addInput(form, "名称", product == null ? "" : product.productName, false);
        Spinner categoryInput = addSpinner(form, "分类", new String[]{"服务", "餐饮", "猫咖用品"}, product == null ? 0 : product.category);
        TextInputEditText priceInput = addInput(form, "价格", product == null || product.price == null ? "" : product.price.toPlainString(), false);
        TextInputEditText stockInput = addInput(form, "库存", product == null ? "" : String.valueOf(product.stockQuantity), false);
        TextInputEditText imageInput = addInput(form, "图片 URL 或相对路径", product == null ? "" : product.imageUrl, false);
        TextInputEditText descInput = addInput(form, "描述", product == null ? "" : product.description, true);
        Spinner statusInput = addSpinner(form, "状态", new String[]{"已下架", "在售"}, product == null ? 1 : product.status);

        new MaterialAlertDialogBuilder(this)
                .setTitle(product == null ? "新增商品" : "编辑商品")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    ProductWriteRequest request = productRequest(nameInput, categoryInput, priceInput, stockInput, imageInput, descInput, statusInput);
                    if (request == null) {
                        return;
                    }
                    if (product == null) {
                        createProduct(request);
                    } else {
                        updateProduct(product.productId, request, "商品已更新");
                    }
                })
                .show();
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

    private void createProduct(ProductWriteRequest request) {
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminCreateProduct(request), new ApiCallback<ProductDetail>() {
            @Override
            public void onSuccess(ProductDetail data) {
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
        NetworkHelper.enqueue(this, ApiClient.getService(this).adminUpdateProduct(productId, request), new ApiCallback<ProductDetail>() {
            @Override
            public void onSuccess(ProductDetail data) {
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
        input.setSingleLine(!multiLine);
        if (multiLine) {
            input.setMinLines(2);
            input.setMaxLines(4);
        }
        layout.addView(input);
        form.addView(layout);
        return input;
    }

    private Spinner addSpinner(LinearLayout form, String label, String[] options, int selected) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));
        spinner.setSelection(Math.max(0, Math.min(selected, options.length - 1)));
        spinner.setPrompt(label);
        form.addView(spinner);
        return spinner;
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
