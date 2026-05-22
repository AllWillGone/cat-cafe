package com.catcafe.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.catcafe.app.R;
import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.SessionManager;
import com.catcafe.app.model.UserDetail;
import com.catcafe.app.network.ApiCallback;
import com.catcafe.app.network.ApiClient;
import com.catcafe.app.network.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Map;

public class MineFragment extends Fragment {
    private SessionManager sessionManager;
    private View guestPanel;
    private View userPanel;
    private ShapeableImageView avatarImage;
    private TextView userNameText;
    private TextView userTypeText;
    private TextView userIdText;
    private TextView userPhoneText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        sessionManager = new SessionManager(requireContext());

        guestPanel = view.findViewById(R.id.guestPanel);
        userPanel = view.findViewById(R.id.userPanel);
        avatarImage = view.findViewById(R.id.mineAvatarImage);
        userNameText = view.findViewById(R.id.mineUserName);
        userTypeText = view.findViewById(R.id.mineUserType);
        userIdText = view.findViewById(R.id.mineUserId);
        userPhoneText = view.findViewById(R.id.mineUserPhone);

        MaterialButton loginButton = view.findViewById(R.id.mineLoginButton);
        MaterialButton registerButton = view.findViewById(R.id.mineRegisterButton);
        MaterialButton logoutButton = view.findViewById(R.id.mineLogoutButton);
        MaterialButton profileButton = view.findViewById(R.id.mineProfileButton);
        MaterialButton passwordButton = view.findViewById(R.id.minePasswordButton);
        MaterialButton deleteAccountButton = view.findViewById(R.id.mineDeleteAccountButton);

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AuthActivity.class)));
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AuthActivity.class).putExtra(AuthActivity.EXTRA_MODE, AuthActivity.MODE_REGISTER)));
        logoutButton.setOnClickListener(v -> {
            sessionManager.clear();
            renderState();
        });
        profileButton.setOnClickListener(v -> startActivity(ProfileActivity.intent(requireContext())));
        passwordButton.setOnClickListener(v -> startActivity(PasswordActivity.intent(requireContext())));
        deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());

        renderState();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderState();
    }

    private void renderState() {
        boolean loggedIn = sessionManager.isLoggedIn();
        guestPanel.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        userPanel.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (!loggedIn) {
            Glide.with(this).clear(avatarImage);
            return;
        }
        userNameText.setText(sessionManager.getUserName());
        userTypeText.setText(sessionManager.isAdmin() ? "管理员账号" : "普通用户账号");
        userIdText.setText(String.valueOf(sessionManager.getUserId()));
        userPhoneText.setText(emptyToDefault(sessionManager.getUserPhone(), "未设置"));
        loadAvatar(sessionManager.getUserAvatar());
        refreshUserDetail();
    }

    private void refreshUserDetail() {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).getMe(), new ApiCallback<UserDetail>() {
            @Override
            public void onSuccess(UserDetail data) {
                if (!isAdded()) {
                    return;
                }
                sessionManager.saveUserDetail(data);
                userNameText.setText(data.userName);
                userTypeText.setText(data.userType == 1 ? "管理员账号" : "普通用户账号");
                userIdText.setText(String.valueOf(data.userId));
                userPhoneText.setText(emptyToDefault(data.userPhone, "未设置"));
                loadAvatar(data.userAvatar);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteAccount() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("注销账号")
                .setMessage("注销后当前账号将无法继续登录。若账号存在历史订单或其他关联数据，后端可能拒绝注销。")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认注销", (dialog, which) -> deleteAccount())
                .show();
    }

    private void deleteAccount() {
        NetworkHelper.enqueue(requireContext(), ApiClient.getService(requireContext()).deleteMe(), new ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (!isAdded()) {
                    return;
                }
                sessionManager.clear();
                renderState();
                Toast.makeText(requireContext(), "账号已注销", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(),
                        message + "，如有历史订单或关联数据，当前账号可能暂不能注销",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAvatar(String path) {
        Glide.with(this)
                .load(AppConfig.buildImageUrl(path))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .circleCrop()
                .into(avatarImage);
    }

    private String emptyToDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
