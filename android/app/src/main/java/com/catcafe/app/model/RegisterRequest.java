package com.catcafe.app.model;

public class RegisterRequest {
    public String userName;
    public String userPassword;
    public String userPhone;

    public RegisterRequest(String userName, String userPassword, String userPhone) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.userPhone = userPhone;
    }
}
