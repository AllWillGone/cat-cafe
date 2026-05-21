package com.catcafe.app.network;

import android.content.Context;

import com.catcafe.app.core.AppConfig;
import com.catcafe.app.core.SessionManager;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 初始化集中放在这里。
 *
 * 初学者提示：
 * Retrofit 负责把 Java 接口方法转换成 HTTP 请求；
 * OkHttp 拦截器负责在每次请求前自动补 token。
 */
public final class ApiClient {
    private static ApiService service;

    private ApiClient() {
    }

    public static ApiService getService(Context context) {
        if (service == null) {
            SessionManager session = new SessionManager(context.getApplicationContext());
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(session))
                    .addInterceptor(logging)
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            service = new Retrofit.Builder()
                    .baseUrl(AppConfig.API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .build()
                    .create(ApiService.class);
        }
        return service;
    }

    private static class AuthInterceptor implements Interceptor {
        private final SessionManager session;

        AuthInterceptor(SessionManager session) {
            this.session = session;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();
            if (session.isLoggedIn()) {
                builder.header("Authorization", session.getBearerToken());
            }
            return chain.proceed(builder.build());
        }
    }
}
