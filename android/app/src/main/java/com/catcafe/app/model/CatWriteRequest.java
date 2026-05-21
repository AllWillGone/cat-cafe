package com.catcafe.app.model;

public class CatWriteRequest {
    public String catName;
    public String breed;
    public String birthday;
    public Integer status;
    public String personality;
    public String photoUrl;
    public String notes;

    public CatWriteRequest(String catName, String breed, String birthday, Integer status,
                           String personality, String photoUrl, String notes) {
        this.catName = catName;
        this.breed = breed;
        this.birthday = birthday;
        this.status = status;
        this.personality = personality;
        this.photoUrl = photoUrl;
        this.notes = notes;
    }
}
