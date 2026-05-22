package com.catcafe.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.catcafe.app.R;
import com.catcafe.app.core.SessionManager;
import com.google.android.material.button.MaterialButton;

public class ActivityFragment extends Fragment {
    private SessionManager sessionManager;
    private View loggedInPanel;
    private View guestPanel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        sessionManager = new SessionManager(requireContext());

        loggedInPanel = view.findViewById(R.id.loggedInPanel);
        guestPanel = view.findViewById(R.id.guestPanel);

        MaterialButton loginButton = view.findViewById(R.id.activityLoginButton);
        MaterialButton registerButton = view.findViewById(R.id.activityRegisterButton);
        MaterialButton ordersButton = view.findViewById(R.id.activityOrdersButton);
        MaterialButton likesButton = view.findViewById(R.id.activityLikesButton);
        MaterialButton commentsButton = view.findViewById(R.id.activityCommentsButton);

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AuthActivity.class)));
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AuthActivity.class).putExtra(AuthActivity.EXTRA_MODE, AuthActivity.MODE_REGISTER)));
        ordersButton.setOnClickListener(v -> startActivity(OrderListActivity.intent(requireContext())));
        likesButton.setOnClickListener(v -> startActivity(LikesActivity.intent(requireContext())));
        commentsButton.setOnClickListener(v -> startActivity(MyCommentsActivity.intent(requireContext())));

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
        loggedInPanel.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
    }
}
