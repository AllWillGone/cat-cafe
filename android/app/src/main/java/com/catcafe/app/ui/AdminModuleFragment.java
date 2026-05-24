package com.catcafe.app.ui;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.AdminUserListItem;
import com.catcafe.app.model.AdminUserUpdateRequest;
import com.catcafe.app.model.BatchOrderResponse;
import com.catcafe.app.model.CatDetail;
import com.catcafe.app.model.CatWriteRequest;
import com.catcafe.app.model.CommentAuditRequest;
import com.catcafe.app.model.CommentDetail;
import com.catcafe.app.model.OrderDetailItem;
import com.catcafe.app.model.OrderUpdateRequest;
import com.catcafe.app.model.PaginatedCats;
import com.catcafe.app.model.PaginatedComments;
import com.catcafe.app.model.PaginatedOrders;
import com.catcafe.app.model.PaginatedProducts;
import com.catcafe.app.model.PaginatedUsers;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.model.ProductWriteRequest;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.util.UiText;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminModuleFragment extends Fragment {
    public static final int MODULE_COMMENTS = 0;
    public static final int MODULE_ORDERS = 1;
    public static final int MODULE_PRODUCTS = 2;
    public static final int MODULE_CATS = 3;
    public static final int MODULE_USERS = 4;
    private static final String ARG_MODULE = "module";

    private final String[] commentFilters = {"全部评论", "待审核", "已通过", "已拒绝"};
    private final String[] orderFilters = {"全部订单", "未支付", "已支付", "待取货", "已完成", "已取消"};
    private final String[] productFilters = {"全部商品", "服务", "餐饮", "猫咖用品", "在售", "已下架"};
    private final String[] catFilters = {"全部猫咪", "休息中", "在岗中"};
    private final String[] userFilters = {"全部用户", "普通用户", "管理员"};
    private final String[] orderStatusOptions = {"未支付", "已支付", "待取货", "已完成", "已取消"};
    private final String[] orderStatusDescriptions = {
            "客户已下单，等待支付",
            "已完成付款，等待备货",
            "商品已备好，等待取货",
            "订单已完成，可归档",
            "订单已取消，库存已处理"
    };

    private int module;
    private int page;
    private int total;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private Spinner filterSpinner;
    private TextInputEditText keywordInput;
    private TextInputLayout keywordLayout;
    private MaterialButton addButton;
    private MaterialButton prevButton;
    private MaterialButton nextButton;
    private TextView pageText;
    private AdminManageAdapter<?> adapter;
    private SessionManager sessionManager;
    private ActivityResultLauncher<String> pickProductImage;
    private ActivityResultLauncher<String> pickCatImage;
    private TextInputEditText activeImageInput;
    private ImageView activeImagePreview;

    public static AdminModuleFragment newInstance(int module) {
        AdminModuleFragment fragment = new AdminModuleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODULE, module);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        module = getArguments() == null ? MODULE_COMMENTS : getArguments().getInt(ARG_MODULE, MODULE_COMMENTS);
        pickProductImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && activeImageInput != null && activeImagePreview != null) {
                AdminImageUploadHelper.upload(requireActivity(), uri, "product", "product", activeImageInput, activeImagePreview);
            }
        });
        pickCatImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && activeImageInput != null && activeImagePreview != null) {
                AdminImageUploadHelper.upload(requireActivity(), uri, "cat", "cat", activeImageInput, activeImagePreview);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_admin_list, container, false);
        sessionManager = new SessionManager(requireContext());
        refreshLayout = view.findViewById(R.id.adminRefresh);
        recyclerView = view.findViewById(R.id.adminRecycler);
        filterSpinner = view.findViewById(R.id.adminFilterSpinner);
        keywordInput = view.findViewById(R.id.adminKeywordInput);
        keywordLayout = view.findViewById(R.id.adminKeywordLayout);
        addButton = view.findViewById(R.id.adminAddButton);
        prevButton = view.findViewById(R.id.adminPrevButton);
        nextButton = view.findViewById(R.id.adminNextButton);
        pageText = view.findViewById(R.id.adminPageText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        view.findViewById(R.id.adminSearchButton).setOnClickListener(v -> {
            page = 0;
            loadData();
        });
        keywordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                page = 0;
                loadData();
                return true;
            }
            return false;
        });
        refreshLayout.setOnRefreshListener(this::loadData);
        prevButton.setOnClickListener(v -> {
            if (page > 0) {
                page--;
                loadData();
            }
        });
        nextButton.setOnClickListener(v -> {
            if ((page + 1) * AppConfig.PAGE_LIMIT < total) {
                page++;
                loadData();
            }
        });

        configure(view.findViewById(R.id.adminToolbar));
        loadData();
        return view;
    }

    private void configure(MaterialToolbar toolbar) {
        addButton.setVisibility(View.GONE);
        switch (module) {
            case MODULE_ORDERS:
                toolbar.setTitle("订单管理");
                keywordLayout.setHint("批次号或联系人搜索");
                setFilterLabels(orderFilters);
                AdminManageAdapter<BatchOrderResponse> orderAdapter = new AdminManageAdapter<>(this::bindOrder);
                adapter = orderAdapter;
                recyclerView.setAdapter(orderAdapter);
                break;
            case MODULE_PRODUCTS:
                toolbar.setTitle("商品管理");
                keywordLayout.setHint("商品名搜索");
                setFilterLabels(productFilters);
                addButton.setVisibility(View.VISIBLE);
                addButton.setText("新增商品");
                addButton.setOnClickListener(v -> showProductForm(null));
                AdminManageAdapter<ProductDetail> productAdapter = new AdminManageAdapter<>(this::bindProduct);
                adapter = productAdapter;
                recyclerView.setAdapter(productAdapter);
                break;
            case MODULE_CATS:
                toolbar.setTitle("猫咪管理");
                keywordLayout.setHint("猫名、品种、性格搜索");
                setFilterLabels(catFilters);
                addButton.setVisibility(View.VISIBLE);
                addButton.setText("新增猫咪");
                addButton.setOnClickListener(v -> showCatForm(null));
                AdminManageAdapter<CatDetail> catAdapter = new AdminManageAdapter<>(this::bindCat);
                adapter = catAdapter;
                recyclerView.setAdapter(catAdapter);
                break;
            case MODULE_USERS:
                toolbar.setTitle("用户管理");
                keywordLayout.setHint("用户名或手机号搜索");
                setFilterLabels(userFilters);
                AdminManageAdapter<AdminUserListItem> userAdapter = new AdminManageAdapter<>(this::bindUser);
                adapter = userAdapter;
                recyclerView.setAdapter(userAdapter);
                break;
            case MODULE_COMMENTS:
            default:
                toolbar.setTitle("评论管理");
                keywordLayout.setHint("用户名、评论ID、内容搜索");
                setFilterLabels(commentFilters);
                AdminManageAdapter<CommentDetail> commentAdapter = new AdminManageAdapter<>(this::bindComment);
                adapter = commentAdapter;
                recyclerView.setAdapter(commentAdapter);
                break;
        }
    }

    private void loadData() {
        if (!isAdded()) {
            return;
        }
        switch (module) {
            case MODULE_ORDERS:
                loadOrders();
                break;
            case MODULE_PRODUCTS:
                loadProducts();
                break;
            case MODULE_CATS:
                loadCats();
                break;
            case MODULE_USERS:
                loadUsers();
                break;
            case MODULE_COMMENTS:
            default:
                loadComments();
                break;
        }
    }

    private void loadComments() {
        showLoading(true);
        Integer status = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(requireContext(),
                ApiClient.getService(requireContext()).adminGetComments(status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedComments>() {
                    @Override
                    public void onSuccess(PaginatedComments data) {
                        if (!isAdded()) return;
                        showLoading(false);
                        updatePaging(data.total);
                        submit(data.items);
                    }

                    @Override
                    public void onError(String message) {
                        showLoading(false);
                        toast(message);
                    }
                });
    }

    private void bindComment(AdminManageAdapter.VH holder, CommentDetail comment) {
        holder.title.setText("#" + comment.commentId + " · " + commentStatusText(comment.auditStatus));
        holder.meta.setText((comment.userName == null ? "未知用户" : comment.userName) + " · " + targetText(comment));
        holder.body.setText(comment.content);
        holder.footer.setText(comment.publishTime == null ? "" : comment.publishTime);
        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("通过");
        holder.primaryButton.setEnabled(comment.auditStatus != 1);
        holder.primaryButton.setOnClickListener(v -> audit(comment.commentId, 1));
        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText("拒绝");
        holder.secondaryButton.setEnabled(comment.auditStatus != 2);
        holder.secondaryButton.setOnClickListener(v -> audit(comment.commentId, 2));
        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDeleteComment(comment));
    }

    private void audit(long commentId, int status) {
        NetworkHelper.enqueue(requireContext(),
                ApiClient.getService(requireContext()).adminAuditComment(commentId, new CommentAuditRequest(status)),
                new ApiCallback<CommentDetail>() {
                    @Override
                    public void onSuccess(CommentDetail data) {
                        toast(status == 1 ? "评论已通过" : "评论已拒绝");
                        loadData();
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }

    private void confirmDeleteComment(CommentDetail comment) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除评论")
                .setMessage("确认删除这条评论？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteComment(comment.commentId))
                .show();
    }

    private void deleteComment(long commentId) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).deleteComment(commentId), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                toast("评论已删除");
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void loadOrders() {
        showLoading(true);
        Integer status = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(requireContext(),
                ApiClient.getService(requireContext()).adminGetOrders(status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedOrders>() {
                    @Override
                    public void onSuccess(PaginatedOrders data) {
                        if (!isAdded()) return;
                        showLoading(false);
                        updatePaging(data.total);
                        submit(data.items);
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
        holder.primaryButton.setOnClickListener(v -> showOrderDetail(order));
        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText("状态");
        holder.secondaryButton.setOnClickListener(v -> showOrderStatusDialog(order));
        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDeleteOrder(order));
    }

    private void showOrderDetail(BatchOrderResponse order) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(UiText.batchLabel(order))
                .setMessage(orderDetailText(order))
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showOrderStatusDialog(BatchOrderResponse order) {
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = dimenPadding();
        content.setPadding(padding, 0, padding, 0);

        TextView summary = new TextView(requireContext());
        summary.setText(UiText.batchLabel(order) + "\n" + order.userName + " · " + UiText.price(order.totalAmount));
        summary.setTextColor(color(R.color.cat_muted));
        summary.setTextSize(13);
        summary.setLineSpacing(dp(2), 1f);
        content.addView(summary);

        final int[] selectedStatus = {Math.max(0, Math.min(order.orderStatus, orderStatusOptions.length - 1))};
        List<MaterialCardView> cards = new ArrayList<>();
        List<RadioButton> radioButtons = new ArrayList<>();
        for (int i = 0; i < orderStatusOptions.length; i++) {
            content.addView(createStatusCard(i, selectedStatus, cards, radioButtons));
        }
        applyStatusSelection(cards, radioButtons, selectedStatus[0]);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("修改订单状态")
                .setView(content)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) ->
                        updateOrder(order, new OrderUpdateRequest(selectedStatus[0], null, null, null)))
                .show();
    }

    private MaterialCardView createStatusCard(int status, int[] selectedStatus,
                                              List<MaterialCardView> cards,
                                              List<RadioButton> radioButtons) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(8);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(8));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setClickable(true);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        RadioButton radioButton = new RadioButton(requireContext());
        radioButton.setClickable(false);
        radioButton.setFocusable(false);
        row.addView(radioButton);

        LinearLayout textColumn = new LinearLayout(requireContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(8);
        textColumn.setLayoutParams(textParams);

        TextView title = new TextView(requireContext());
        title.setText(orderStatusOptions[status]);
        title.setTextColor(color(R.color.cat_text));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        textColumn.addView(title);

        TextView description = new TextView(requireContext());
        description.setText(orderStatusDescriptions[status]);
        description.setTextColor(color(R.color.cat_muted));
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
            cards.get(i).setCardBackgroundColor(color(selected ? R.color.cat_surface_alt : R.color.white));
            cards.get(i).setStrokeColor(color(selected ? R.color.cat_primary : R.color.cat_border));
            radioButtons.get(i).setChecked(selected);
        }
    }

    private void confirmDeleteOrder(BatchOrderResponse order) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除订单")
                .setMessage("确定删除该订单吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteOrder(order))
                .show();
    }

    private void updateOrder(BatchOrderResponse order, OrderUpdateRequest request) {
        Long orderId = firstOrderId(order);
        if (orderId == null) {
            toast("订单明细为空");
            return;
        }
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).updateOrder(orderId, request), new ApiCallback<BatchOrderResponse>() {
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

    private void deleteOrder(BatchOrderResponse order) {
        Long orderId = firstOrderId(order);
        if (orderId == null) {
            toast("订单明细为空");
            return;
        }
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).deleteOrder(orderId), new ApiCallback<Map<String, Object>>() {
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

    private void loadProducts() {
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
        NetworkHelper.enqueue(requireContext(),
                ApiClient.getService(requireContext()).adminGetProducts(category, status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedProducts>() {
                    @Override
                    public void onSuccess(PaginatedProducts data) {
                        if (!isAdded()) return;
                        showLoading(false);
                        updatePaging(data.total);
                        submit(data.items);
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
        holder.primaryButton.setOnClickListener(v -> showProductForm(product));
        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText(product.status == 1 ? "下架" : "上架");
        holder.secondaryButton.setOnClickListener(v -> {
            if (product.status == 1) {
                delistProduct(product);
            } else {
                updateProduct(product.productId, new ProductWriteRequest(null, null, null, null, null, null, 1), "商品已上架", null);
            }
        });
        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDeleteProduct(product));
    }

    private void showProductForm(ProductDetail product) {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dimenPadding(), 0, dimenPadding(), 0);
        TextInputEditText nameInput = addInput(form, "名称", product == null ? "" : product.productName, false);
        Spinner categoryInput = addSpinner(form, "分类", new String[]{"服务", "餐饮", "猫咖用品"}, product == null ? 0 : product.category);
        TextInputEditText priceInput = addInput(form, "价格", product == null || product.price == null ? "" : product.price.toPlainString(), false);
        TextInputEditText stockInput = addInput(form, "库存", product == null ? "" : String.valueOf(product.stockQuantity), false);
        TextInputEditText imageInput = addInput(form, "图片 URL 或相对路径", product == null ? "" : product.imageUrl, false);
        ImageView imagePreview = addImagePreview(form, product == null ? "" : product.imageUrl);
        addUploadButton(form, "上传商品图片", imageInput, imagePreview, true);
        TextInputEditText descInput = addInput(form, "描述", product == null ? "" : product.description, true);
        Spinner statusInput = addSpinner(form, "状态", new String[]{"已下架", "在售"}, product == null ? 1 : product.status);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(product == null ? "新增商品" : "编辑商品")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            ProductWriteRequest request = productRequest(nameInput, categoryInput, priceInput, stockInput, imageInput, descInput, statusInput);
            if (request == null) return;
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
            return new ProductWriteRequest(name, categoryInput.getSelectedItemPosition(), new BigDecimal(priceText),
                    Integer.parseInt(stockText), image, textOf(descInput), statusInput.getSelectedItemPosition());
        } catch (NumberFormatException ex) {
            toast("价格或库存格式不正确");
            return null;
        }
    }

    private void createProduct(ProductWriteRequest request, AlertDialog dialog) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminCreateProduct(request), new ApiCallback<ProductDetail>() {
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

    private void updateProduct(long productId, ProductWriteRequest request, String successMessage, @Nullable AlertDialog dialog) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminUpdateProduct(productId, request), new ApiCallback<ProductDetail>() {
            @Override
            public void onSuccess(ProductDetail data) {
                if (dialog != null) dialog.dismiss();
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
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminDelistProduct(product.productId), new ApiCallback<Map<String, Object>>() {
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

    private void confirmDeleteProduct(ProductDetail product) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除商品")
                .setMessage("有历史订单时后端会改为下架保留。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteProduct(product.productId))
                .show();
    }

    private void deleteProduct(long productId) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminDeleteProduct(productId), new ApiCallback<Map<String, Object>>() {
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

    private void loadCats() {
        showLoading(true);
        Integer status = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(requireContext(),
                ApiClient.getService(requireContext()).adminGetCats(status, keyword(), skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedCats>() {
                    @Override
                    public void onSuccess(PaginatedCats data) {
                        if (!isAdded()) return;
                        showLoading(false);
                        updatePaging(data.total);
                        submit(data.items);
                    }

                    @Override
                    public void onError(String message) {
                        showLoading(false);
                        toast(message);
                    }
                });
    }

    private void bindCat(AdminManageAdapter.VH holder, CatDetail cat) {
        holder.title.setText(cat.catName);
        holder.meta.setText("#" + cat.catId + " · " + cat.breed + " · " + UiText.catStatus(cat.status));
        holder.body.setText("生日 " + cat.birthday + "\n" + cat.personality);
        holder.footer.setText(cat.notes == null || cat.notes.trim().isEmpty() ? "暂无备注" : cat.notes);
        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("编辑");
        holder.primaryButton.setOnClickListener(v -> showCatForm(cat));
        holder.secondaryButton.setVisibility(View.VISIBLE);
        holder.secondaryButton.setText(cat.status == 1 ? "休息" : "在岗");
        holder.secondaryButton.setOnClickListener(v -> updateCat(cat.catId,
                new CatWriteRequest(null, null, null, cat.status == 1 ? 0 : 1, null, null, null),
                "状态已更新", null));
        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setOnClickListener(v -> confirmDeleteCat(cat));
    }

    private void showCatForm(CatDetail cat) {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dimenPadding(), 0, dimenPadding(), 0);
        TextInputEditText nameInput = addInput(form, "名字", cat == null ? "" : cat.catName, false);
        TextInputEditText breedInput = addInput(form, "品种", cat == null ? "" : cat.breed, false);
        TextInputEditText birthdayInput = addInput(form, "生日 yyyy-MM-dd", cat == null ? "" : cat.birthday, false);
        Spinner statusInput = addSpinner(form, new String[]{"休息中", "在岗中"}, cat == null ? 1 : cat.status);
        TextInputEditText personalityInput = addInput(form, "性格", cat == null ? "" : cat.personality, true);
        TextInputEditText photoInput = addInput(form, "照片 URL 或相对路径", cat == null ? "" : cat.photoUrl, false);
        ImageView photoPreview = addImagePreview(form, cat == null ? "" : cat.photoUrl);
        addUploadButton(form, "上传猫咪照片", photoInput, photoPreview, false);
        TextInputEditText notesInput = addInput(form, "备注", cat == null ? "" : cat.notes, true);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(cat == null ? "新增猫咪" : "编辑猫咪")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            CatWriteRequest request = catRequest(nameInput, breedInput, birthdayInput, statusInput, personalityInput, photoInput, notesInput);
            if (request == null) return;
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
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminCreateCat(request), new ApiCallback<CatDetail>() {
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

    private void updateCat(long catId, CatWriteRequest request, String successMessage, @Nullable AlertDialog dialog) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminUpdateCat(catId, request), new ApiCallback<CatDetail>() {
            @Override
            public void onSuccess(CatDetail data) {
                if (dialog != null) dialog.dismiss();
                toast(successMessage);
                loadData();
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void confirmDeleteCat(CatDetail cat) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除猫咪")
                .setMessage("删除不会自动删除历史评论。确认删除这只猫咪？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteCat(cat.catId))
                .show();
    }

    private void deleteCat(long catId) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminDeleteCat(catId), new ApiCallback<Map<String, Object>>() {
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

    private void loadUsers() {
        showLoading(true);
        Integer userType = filterSpinner.getSelectedItemPosition() == 0 ? null : filterSpinner.getSelectedItemPosition() - 1;
        NetworkHelper.enqueue(requireContext(),
                ApiClient.getService(requireContext()).adminGetUsers(keyword(), userType, "userId", "desc", skip(), AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedUsers>() {
                    @Override
                    public void onSuccess(PaginatedUsers data) {
                        if (!isAdded()) return;
                        showLoading(false);
                        updatePaging(data.total);
                        submit(data.items);
                    }

                    @Override
                    public void onError(String message) {
                        showLoading(false);
                        toast(message);
                    }
                });
    }

    private void bindUser(AdminManageAdapter.VH holder, AdminUserListItem user) {
        holder.title.setText(user.userName);
        holder.meta.setText("#" + user.userId + " · " + (user.userType == 1 ? "管理员" : "普通用户"));
        holder.body.setText("手机号：" + empty(user.userPhone, "未设置") + " · 性别：" + genderText(user.gender));
        holder.footer.setText("注册时间：" + empty(user.registerTime, "未知"));
        holder.primaryButton.setVisibility(View.VISIBLE);
        holder.primaryButton.setText("编辑");
        holder.primaryButton.setOnClickListener(v -> showUserForm(user));
        holder.dangerButton.setVisibility(View.VISIBLE);
        holder.dangerButton.setText("删除");
        holder.dangerButton.setEnabled(user.userId != sessionManager.getUserId());
        holder.dangerButton.setOnClickListener(v -> confirmDeleteUser(user));
    }

    private void showUserForm(AdminUserListItem user) {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dimenPadding(), 0, dimenPadding(), 0);
        TextInputEditText nameInput = addInput(form, "用户名", user.userName, false);
        TextInputEditText phoneInput = addInput(form, "手机号", user.userPhone, false);
        TextInputEditText avatarInput = addInput(form, "头像 URL 或相对路径", user.userAvatar, false);
        Spinner genderInput = addSpinner(form, new String[]{"不修改", "男", "女"}, user.gender == null ? 0 : user.gender);
        TextInputEditText birthdayInput = addInput(form, "生日 yyyy-MM-dd，可留空不修改", "", false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
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
            updateUser(user.userId, new AdminUserUpdateRequest(textOf(nameInput), gender, birthday, textOf(phoneInput), textOf(avatarInput)), dialog);
        });
    }

    private void updateUser(long userId, AdminUserUpdateRequest request, AlertDialog dialog) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminUpdateUser(userId, request), new ApiCallback<AdminUserListItem>() {
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

    private void confirmDeleteUser(AdminUserListItem user) {
        if (user.userId == sessionManager.getUserId()) {
            toast("不能删除自己的账号");
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除用户")
                .setMessage("有关联订单的用户会由后端拒绝删除。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteUser(user.userId))
                .show();
    }

    private void deleteUser(long userId) {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).adminDeleteUser(userId), new ApiCallback<Map<String, Object>>() {
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

    private ImageView addImagePreview(LinearLayout form, String path) {
        ImageView preview = new ImageView(requireContext());
        int size = dp(96);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.topMargin = dp(8);
        params.bottomMargin = dp(8);
        preview.setLayoutParams(params);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        form.addView(preview);
        AdminImageUploadHelper.loadPreview(requireActivity(), preview, path);
        return preview;
    }

    private void addUploadButton(LinearLayout form, String label, TextInputEditText input, ImageView preview, boolean product) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(label);
        button.setOnClickListener(v -> {
            activeImageInput = input;
            activeImagePreview = preview;
            if (product) {
                pickProductImage.launch("image/*");
            } else {
                pickCatImage.launch("image/*");
            }
        });
        form.addView(button);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Activity activity = getActivity();
                if (activity != null) {
                    AdminImageUploadHelper.loadPreview(activity, preview, s == null ? "" : s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private TextInputEditText addInput(LinearLayout form, String hint, String value, boolean multiLine) {
        TextInputLayout layout = new TextInputLayout(requireContext());
        layout.setHint(hint);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setText(value == null ? "" : value);
        input.setTextColor(color(R.color.cat_text));
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

    private Spinner addSpinner(LinearLayout form, String[] options, int selected) {
        return addSpinner(form, null, options, selected);
    }

    private Spinner addSpinner(LinearLayout form, @Nullable String label, String[] options, int selected) {
        Spinner spinner = new Spinner(requireContext());
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, options));
        spinner.setSelection(Math.max(0, Math.min(selected, options.length - 1)));
        if (label != null) {
            spinner.setPrompt(label);
        }
        form.addView(spinner);
        return spinner;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void submit(List<?> items) {
        ((AdminManageAdapter) adapter).submitList(items);
    }

    private void setFilterLabels(String[] labels) {
        filterSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels));
    }

    private String keyword() {
        String text = keywordInput.getText() == null ? "" : keywordInput.getText().toString().trim();
        return text.isEmpty() ? null : text;
    }

    private int skip() {
        return page * AppConfig.PAGE_LIMIT;
    }

    private void updatePaging(int total) {
        this.total = total;
        int from = total == 0 ? 0 : skip() + 1;
        int to = Math.min(skip() + AppConfig.PAGE_LIMIT, total);
        pageText.setText(from + "-" + to + " / " + total);
        prevButton.setEnabled(page > 0);
        nextButton.setEnabled((page + 1) * AppConfig.PAGE_LIMIT < total);
    }

    private void showLoading(boolean loading) {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(loading);
        }
    }

    private String productSummary(BatchOrderResponse order) {
        if (order.items == null || order.items.isEmpty()) return "暂无商品明细";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < order.items.size(); i++) {
            OrderDetailItem item = order.items.get(i);
            if (i > 0) builder.append("、");
            builder.append(item.productName).append(" x").append(item.productQuantity);
        }
        return builder.toString();
    }

    private String orderDetailText(BatchOrderResponse order) {
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
                builder.append("\n#").append(item.orderId).append(" ")
                        .append(item.productName).append(" x").append(item.productQuantity)
                        .append("  ").append(UiText.price(item.totalAmount));
            }
        }
        return builder.toString();
    }

    private Long firstOrderId(BatchOrderResponse order) {
        return order.items == null || order.items.isEmpty() ? null : order.items.get(0).orderId;
    }

    private String commentStatusText(int status) {
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

    private String targetText(CommentDetail comment) {
        return (comment.targetType == 0 ? "商品 #" : "猫咪 #") + comment.targetId;
    }

    private String genderText(Integer gender) {
        if (gender == null) return "未设置";
        return gender == 1 ? "男" : "女";
    }

    private String empty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private int dimenPadding() {
        return getResources().getDimensionPixelSize(R.dimen.order_dialog_padding);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int color(int colorRes) {
        return requireContext().getColor(colorRes);
    }

    private void toast(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
