package com.catcafe.app.network;

public interface ApiCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}
