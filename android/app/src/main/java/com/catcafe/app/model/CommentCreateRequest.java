package com.catcafe.app.model;

public class CommentCreateRequest {
    public int targetType;
    public long targetId;
    public String content;

    public CommentCreateRequest(int targetType, long targetId, String content) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.content = content;
    }
}
