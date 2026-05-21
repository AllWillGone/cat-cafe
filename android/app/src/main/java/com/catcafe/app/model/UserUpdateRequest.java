package com.catcafe.app.model;

public class UserUpdateRequest {
    public String userName;
    public Integer gender;
    public String birthday;
    public String userPhone;
    public String userAvatar;

    public UserUpdateRequest(String userName, Integer gender, String birthday, String userPhone, String userAvatar) {
        this.userName = userName;
        this.gender = gender;
        this.birthday = birthday;
        this.userPhone = userPhone;
        this.userAvatar = userAvatar;
    }
}
