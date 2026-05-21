package com.catcafe.app.model;

public class ResetPasswordByPhoneRequest {
    public String userPhone;
    public String code;
    public String newPassword;

    public ResetPasswordByPhoneRequest(String userPhone, String code, String newPassword) {
        this.userPhone = userPhone;
        this.code = code;
        this.newPassword = newPassword;
    }
}
