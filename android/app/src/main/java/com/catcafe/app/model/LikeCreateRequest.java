package com.catcafe.app.model;

public class LikeCreateRequest {
    public int likeType;
    public long objectId;
    public String linkUrl;

    public LikeCreateRequest(int likeType, long objectId, String linkUrl) {
        this.likeType = likeType;
        this.objectId = objectId;
        this.linkUrl = linkUrl;
    }
}
