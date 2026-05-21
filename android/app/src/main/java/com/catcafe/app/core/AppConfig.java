package com.catcafe.app.core;

import com.catcafe.app.BuildConfig;

/**
 * AppConfig 集中保存后端地址。
 *
 * 初学者提示：
 * 不要在每个页面里分别写地址。
 * 现在地址由 build.gradle 里的 BuildConfig 统一提供。
 */
public final class AppConfig {
    public static final String API_BASE_URL = BuildConfig.API_BASE_URL;
    public static final String IMAGE_BASE_URL = BuildConfig.IMAGE_BASE_URL;
    public static final int PAGE_LIMIT = 20;

    private AppConfig() {
        // 工具类不需要被 new 出来。
    }

    public static String buildImageUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (path.startsWith("/")) {
            return IMAGE_BASE_URL + path;
        }
        return IMAGE_BASE_URL + "/" + path;
    }
}
