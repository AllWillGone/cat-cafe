package com.catcafe.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.catcafe.app.MainActivity;
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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private SessionManager sessionManager;
    private android.content.Context appContext;
    private TextView welcomeText;
    private TextView cartBadge;
    private CatAdapter catAdapter;
    private ProductAdapter productAdapter;
    private List<ProductDetail> serviceProducts = new ArrayList<>();
    private List<ProductDetail> allProducts = new ArrayList<>();
    private boolean serviceProductsLoaded;
    private boolean allProductsLoaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        appContext = requireContext().getApplicationContext();
        sessionManager = new SessionManager(requireContext());
        welcomeText = view.findViewById(R.id.welcomeText);
        cartBadge = view.findViewById(R.id.homeCartBadge);
        MaterialButton loginButton = view.findViewById(R.id.homeLoginButton);
        MaterialButton cartButton = view.findViewById(R.id.homeCartButton);
        MaterialButton adminButton = view.findViewById(R.id.homeAdminButton);

        RecyclerView catList = view.findViewById(R.id.homeCatList);
        RecyclerView productList = view.findViewById(R.id.homeProductList);
        catAdapter = new CatAdapter(requireContext(), this::openCatDetail,
                (cat, countView, button) -> {
                    if (sessionManager.isAdmin()) {
                        button.setVisibility(View.GONE);
                    } else {
                        new LikeController(requireActivity(), 2, cat.catId, cat.likeCount, countView, button).bind();
                    }
                });
        productAdapter = new ProductAdapter(requireContext(), this::openProductDetail, cartBinding(),
                (product, countView, button) -> {
                    if (sessionManager.isAdmin()) {
                        button.setVisibility(View.GONE);
                    } else {
                        new LikeController(requireActivity(), 0, product.productId, product.likeCount, countView, button).bind();
                    }
                });

        catList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        catList.setAdapter(catAdapter);
        productList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        productList.setAdapter(productAdapter);

        loginButton.setOnClickListener(v -> ((MainActivity) requireActivity()).openMinePage());
        cartButton.setOnClickListener(v -> {
            if (sessionManager.isAdmin()) {
                Toast.makeText(requireContext(), "管理员账号不参与顾客操作", Toast.LENGTH_SHORT).show();
                return;
            }
            ((MainActivity) requireActivity()).openCartPage();
        });
        adminButton.setOnClickListener(v -> startActivity(new android.content.Intent(requireContext(), AdminAuthActivity.class)));

        refreshWelcome();
        refreshCartCount();
        loadHomeData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshWelcome();
        refreshCartCount();
        if (productAdapter != null) {
            productAdapter.refreshQuantities();
        }
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
                refreshCartCount();
            }

            @Override
            public void onDecrease(ProductDetail product) {
                CartManager cart = CartManager.getInstance();
                cart.setQuantity(product.productId, cart.getQuantity(product.productId) - 1);
                refreshCartCount();
            }
        };
    }

    private void refreshWelcome() {
        if (sessionManager.isLoggedIn()) {
            welcomeText.setText("欢迎回来，" + sessionManager.getUserName());
        } else {
            welcomeText.setText("欢迎来到猫咖");
        }
    }

    private void refreshCartCount() {
        if (sessionManager.isAdmin()) {
            cartBadge.setVisibility(View.GONE);
            return;
        }
        int count = CartManager.getInstance().getCount();
        cartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        cartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private void openCatDetail(CatDetail cat) {
        startActivity(CatDetailActivity.intent(requireContext(), cat.catId));
    }

    private void openProductDetail(ProductDetail product) {
        startActivity(ProductDetailActivity.intent(requireContext(), product.productId));
    }

    private void loadHomeData() {
        NetworkHelper.enqueue(requireContext(),
                ApiClient.getService(appContext).getCats(null, 0, 8),
                new ApiCallback<PaginatedCats>() {
                    @Override
                    public void onSuccess(PaginatedCats data) {
                        if (!isAdded()) {
                            return;
                        }
                        catAdapter.submitList(data.items);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                    }
                });

        NetworkHelper.enqueue(appContext,
                ApiClient.getService(appContext).getProducts(0, null, 0, AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedProducts>() {
                    @Override
                    public void onSuccess(PaginatedProducts data) {
                        if (!isAdded()) {
                            return;
                        }
                        serviceProducts = data.items == null ? new ArrayList<>() : data.items;
                        serviceProductsLoaded = true;
                        renderRecommendations();
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                    }
                });

        NetworkHelper.enqueue(appContext,
                ApiClient.getService(appContext).getProducts(null, null, 0, AppConfig.PAGE_LIMIT),
                new ApiCallback<PaginatedProducts>() {
                    @Override
                    public void onSuccess(PaginatedProducts data) {
                        if (!isAdded()) {
                            return;
                        }
                        allProducts = data.items == null ? new ArrayList<>() : data.items;
                        allProductsLoaded = true;
                        renderRecommendations();
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

    private void renderRecommendations() {
        if (!serviceProductsLoaded || !allProductsLoaded) {
            return;
        }
        productAdapter.submitList(ProductRules.recommendedProducts(serviceProducts, allProducts, 3));
    }
}
