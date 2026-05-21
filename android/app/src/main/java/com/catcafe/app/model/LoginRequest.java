package com.catcafe.app.model;

public class LoginRequest {
    public String userName;
    public String userPhone;
    public String userPassword;

    public static LoginRequest byAccount(String account, String password) {
        LoginRequest request = new LoginRequest();
        if (account != null && account.matches("\\d+")) {
            request.userPhone = account;
        } else {
            request.userName = account;
        }
        request.userPassword = password;
        return request;
    }
}
