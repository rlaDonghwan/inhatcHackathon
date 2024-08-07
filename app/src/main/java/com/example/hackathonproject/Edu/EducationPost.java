package com.example.hackathonproject.Edu;

import java.sql.Timestamp;

public class EducationPost {
    private int postId;
    private String title;
    private String category;
    private String content;
    private String location;
    private int views;
    private Timestamp createdAt;
    private String userName;

    public EducationPost(int postId, String title, String category, String content, String location, int views, Timestamp createdAt, String userName) {
        this.postId = postId;
        this.title = title;
        this.category = category;
        this.content = content;
        this.location = location;
        this.views = views;
        this.createdAt = createdAt;
        this.userName = userName;
    }

    // getter 및 setter 메서드 추가
    public int getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public String getLocation() {
        return location;
    }

    public int getViews() {
        return views;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getUserName() {
        return userName;
    }
}
