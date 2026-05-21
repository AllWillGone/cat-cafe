package com.catcafe.app.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.catcafe.app.model.LoginResponse;
import com.catcafe.app.model.UserDetail;

/**
 * SessionManager 只负责保存/读取登录状态。
 *
 * SharedPreferences 可以理解成 Android 的小型 key-value 文件。
 * 这里保存 token 后，网络层会自动给登录接口加 Authorization 请求头。
 */
public class SessionManager {
    private static final String PREF_NAME = "cat_cafe_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_USER_AVATAR = "userAvatar";
    private static final String KEY_USER_PHONE = "userPhone";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(LoginResponse response) {
        prefs.edit()
                .putString(KEY_TOKEN, response.token)
                .putLong(KEY_USER_ID, response.userId)
                .putString(KEY_USER_NAME, response.userName)
                .putInt(KEY_USER_TYPE, response.userType)
                .apply();
    }

    public void saveUserDetail(UserDetail user) {
        prefs.edit()
                .putLong(KEY_USER_ID, user.userId)
                .putString(KEY_USER_NAME, user.userName)
                .putInt(KEY_USER_TYPE, user.userType)
                .putString(KEY_USER_AVATAR, user.userAvatar)
                .putString(KEY_USER_PHONE, user.userPhone)
                .apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public String getBearerToken() {
        return "Bearer " + getToken();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserAvatar() {
        return prefs.getString(KEY_USER_AVATAR, "");
    }

    public int getUserType() {
        return prefs.getInt(KEY_USER_TYPE, 0);
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0L);
    }

    public boolean isAdmin() {
        return getUserType() == 1;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
