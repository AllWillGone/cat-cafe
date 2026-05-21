package com.catcafe.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.CartManager;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.CatDetail;
import com.catcafe.app.model.PaginatedCats;
import com.catcafe.app.model.PaginatedProducts;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.catcafe.app.ui.adapter.CatAdapter;
import com.catcafe.app.ui.adapter.ProductAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class ServiceFragment extends Fragment {
    private static final int TAB_PRODUCTS = R.id.tabProducts;
    private static final int TAB_CATS = R.id.tabCats;

    private SwipeRefreshLayout refreshLayout;
    private EditText searchInput;
    private TextView ticketSectionTitle;
    private TextView catSectionTitle;
    private TextView cartBadge;
    private RecyclerView ticketList;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ProductAdapter ticketAdapter;
    private CatAdapter catAdapter;
    private SessionManager sessionManager;
    private int currentTab = TAB_PRODUCTS;
    private android.content.Context appContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);
        appContext = requireContext().getApplicationContext();
        sessionManager = new SessionManager(requireContext());
        if (sessionManager.isAdmin()) {
            return createAdminView(inflater, container);
        }
        refreshLayout = view.findViewById(R.id.serviceRefresh);
        searchInput = view.findViewById(R.id.searchInput);
        ticketSectionTitle = view.findViewById(R.id.ticketSectionTitle);
        catSectionTitle = view.findViewById(R.id.catSectionTitle);
        cartBadge = view.findViewById(R.id.serviceCartBadge);
        ticketList = view.findViewById(R.id.catCafeTicketList);
        recyclerView = view.findViewById(R.id.serviceList);
        MaterialButtonToggleGroup tabs = view.findViewById(R.id.serviceTabs);
        MaterialButton cartButton = view.findViewById(R.id.serviceCartButton);

        productAdapter = createProductAdapter();
        ticketAdapter = createProductAdapter();
        catAdapter = new CatAdapter(requireContext(), this::openCatDetail,
                (cat, countView, button) -> new LikeController(requireActivity(), 2, cat.catId, cat.likeCount, countView, button).bind());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ticketList.setLayoutManager(new LinearLayoutManager(requireContext()));
        ticketList.setAdapter(ticketAdapter);

        tabs.check(TAB_PRODUCTS);
        tabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            currentTab = checkedId;
            loadCurrentTab();
        });

        cartButton.setOnClickListener(v -> requireActivity().startActivity(CartActivity.intent(requireContext())));
        refreshCartBadge();

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadCurrentTab();
                return true;
            }
            return false;
        });
        refreshLayout.setOnRefreshListener(this::loadCurrentTab);
        loadCurrentTab();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isAdmin()) {
            return;
        }
        if (productAdapter != null) {
            productAdapter.refreshQuantities();
        }
        if (ticketAdapter != null) {
            ticketAdapter.refreshQuantities();
        }
        refreshCartBadge();
    }

    private ProductAdapter createProductAdapter() {
        return new ProductAdapter(requireContext(), this::openProductDetail, cartBinding(),
                (product, countView, button) -> new LikeController(requireActivity(), 0, product.productId, product.likeCount, countView, button).bind());
    }

    private ProductAdapter.OnCartQuantityChangeListener cartBinding() {
        return new ProductAdapter.OnCartQuantityChangeListener() {
            @Override
            public int getQuantity(ProductDetail product) {
                return CartManager.getInstance().getQuantity(product.productId);
            }

            @Override
            public void onIncrease(ProductDetail product) {
                if (sessionManager.isAdmin()) {
                    Toast.makeText(requireContext(), "管理员账号不参与顾客操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(requireContext(), "请先登录后购买", Toast.LENGTH_SHORT).show();
                    startActivity(new android.content.Intent(requireContext(), AuthActivity.class));
                    return;
                }
                CartManager.getInstance().add(product, 1);
                refreshAdapters();
                refreshCartBadge();
            }

            @Override
            public void onDecrease(ProductDetail product) {
                CartManager cart = CartManager.getInstance();
                cart.setQuantity(product.productId, cart.getQuantity(product.productId) - 1);
                refreshAdapters();
                refreshCartBadge();
            }
        };
    }

    private void refreshAdapters() {
        if (productAdapter != null) {
            productAdapter.refreshQuantities();
        }
        if (ticketAdapter != null) {
            ticketAdapter.refreshQuantities();
        }
    }

    private void refreshCartBadge() {
        if (cartBadge == null) {
            return;
        }
        int count = CartManager.getInstance().getCount();
        cartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        cartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private void loadCurrentTab() {
        if (currentTab == TAB_CATS) {
            showCatCafeMode();
            recyclerView.setAdapter(catAdapter);
            loadCatCafeTickets();
            loadCats();
        } else {
            showProductMode();
            recyclerView.setAdapter(productAdapter);
            loadProducts();
        }
    }

    private void showCatCafeMode() {
        ticketSectionTitle.setVisibility(View.VISIBLE);
        ticketList.setVisibility(View.VISIBLE);
        catSectionTitle.setVisibility(View.VISIBLE);
        searchInput.setHint("搜索猫咪");
    }

    private void showProductMode() {
        ticketSectionTitle.setVisibility(View.GONE);
        ticketList.setVisibility(View.GONE);
        catSectionTitle.setVisibility(View.GONE);
        searchInput.setHint("搜索商品");
    }

    private String keyword() {
        String value = searchInput.getText().toString().trim();
        return value.isEmpty() ? null : value;
    }

    private void openProductDetail(ProductDetail product) {
        startActivity(ProductDetailActivity.intent(requireContext(), product.productId));
    }

    private void openCatDetail(CatDetail cat) {
        startActivity(CatDetailActivity.intent(requireContext(), cat.catId));
    }

    private void loadProducts() {
        refreshLayout.setRefreshing(true);
        NetworkHelper.enqueue(appContext,
                ApiClient.getService(appContext).getProducts(null, keyword(), 0, AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedProducts>() {
                    @Override
                    public void onSuccess(PaginatedProducts data) {
                        if (!isAdded() || refreshLayout == null) {
                            return;
                        }
                        refreshLayout.setRefreshing(false);
                        productAdapter.submitList(ProductRules.productsWithoutTickets(data.items));
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded() || refreshLayout == null) {
                            return;
                        }
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCatCafeTickets() {
        NetworkHelper.enqueue(appContext,
                ApiClient.getService(appContext).getProducts(0, null, 0, AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedProducts>() {
                    @Override
                    public void onSuccess(PaginatedProducts data) {
                        if (!isAdded()) {
                            return;
                        }
                        ticketAdapter.submitList(ProductRules.catCafeTickets(data.items));
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCats() {
        refreshLayout.setRefreshing(true);
        NetworkHelper.enqueue(appContext,
                ApiClient.getService(appContext).getCats(keyword(), true, 0, AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedCats>() {
                    @Override
                    public void onSuccess(PaginatedCats data) {
                        if (!isAdded() || refreshLayout == null) {
                            return;
                        }
                        refreshLayout.setRefreshing(false);
                        catAdapter.submitList(data.items);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded() || refreshLayout == null) {
                            return;
                        }
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private View createAdminView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        view.findViewById(R.id.adminCommentsButton).setOnClickListener(v -> startActivity(AdminCommentsActivity.intent(requireContext())));
        view.findViewById(R.id.adminOrdersButton).setOnClickListener(v -> startActivity(AdminOrdersActivity.intent(requireContext())));
        view.findViewById(R.id.adminProductsButton).setOnClickListener(v -> startActivity(AdminProductsActivity.intent(requireContext())));
        view.findViewById(R.id.adminCatsButton).setOnClickListener(v -> startActivity(AdminCatsActivity.intent(requireContext())));
        view.findViewById(R.id.adminUsersButton).setOnClickListener(v -> startActivity(AdminUsersActivity.intent(requireContext())));
        return view;
    }
}
