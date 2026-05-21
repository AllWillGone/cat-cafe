package com.catcafe.app.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.catcafe.app.core.SessionManager;
import com.catcafe.app.ui.AdminAuthActivity;
import com.catcafe.app.ui.AuthActivity;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NetworkHelper {
    private static final String TAG = "CatCafeNetwork";
    private static long lastLoginRedirectAt;
    private NetworkHelper() {
    }

    public static <T> void enqueue(Context context, Call<T> call, ApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Log.d(TAG, "HTTP " + response.code() + " " + call.request().url());
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    return;
                }
                if (response.code() == 401) {
                    handleUnauthorized(context, call.request().url().encodedPath().startsWith("/api/admin"));
                }
                callback.onError(readErrorMessage(response));
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Log.e(TAG, "Request failed: " + call.request().url(), t);
                callback.onError("网络连接失败，请检查后端服务或网络");
            }
        });
    }

    private static void handleUnauthorized(Context context, boolean adminRequest) {
        Context appContext = context.getApplicationContext();
        new SessionManager(appContext).clear();
        if (context instanceof AuthActivity || context instanceof AdminAuthActivity) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastLoginRedirectAt < 1500L) {
            return;
        }
        lastLoginRedirectAt = now;
        Toast.makeText(appContext, "登录已失效，请重新登录", Toast.LENGTH_SHORT).show();
        Class<?> loginActivity = adminRequest || context.getClass().getSimpleName().startsWith("Admin")
                ? AdminAuthActivity.class
                : AuthActivity.class;
        Intent intent = new Intent(appContext, loginActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (context instanceof Activity) {
            ((Activity) context).startActivity(intent);
        } else {
            appContext.startActivity(intent);
        }
    }

    private static String readErrorMessage(Response<?> response) {
        String fallback = mapStatus(response.code());
        if (response.errorBody() == null) {
            return fallback;
        }
        try {
            ApiError error = new Gson().fromJson(response.errorBody().string(), ApiError.class);
            if (error != null && error.detail != null && !error.detail.trim().isEmpty()) {
                return error.detail;
            }
        } catch (IOException ignored) {
            // 解析失败时使用状态码对应的友好提示。
        }
        return fallback;
    }

    private static String mapStatus(int code) {
        switch (code) {
            case 401:
                return "请先登录";
            case 403:
                return "暂无权限";
            case 404:
                return "内容不存在或已被删除";
            case 409:
                return "已经点过赞了";
            case 422:
                return "请检查填写内容";
            default:
                return "请求失败，请稍后再试";
        }
    }
}
