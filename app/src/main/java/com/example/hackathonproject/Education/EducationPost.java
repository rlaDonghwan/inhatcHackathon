package com.example.hackathonproject.Education;

public class EducationPost {
    private int postId;  // 게시글 ID
    private String title;  // 게시글 제목
    private String category;  // 게시글 카테고리
    private String content;  // 게시글 내용
    private String location;  // 게시글 위치
    private int views;  // 조회수 필드
    private String createdAt;  // 게시글 작성 시간
    private String userName;  // 작성자 이름

    // 생성자: 모든 필드를 초기화
    public EducationPost(int postId, String title, String category, String content, String location, int views, String createdAt, String userName) {
        this.postId = postId;
        this.title = title;
        this.category = category;
        this.content = content;
        this.location = location;
        this.views = views;  // 조회수 초기화
        this.createdAt = createdAt;
        this.userName = userName;
    }

    // getter 메서드: 각 필드에 접근할 수 있는 메서드들

    // 게시글 ID를 반환하는 메서드
    public int getPostId() {
        return postId;
    }

    // 게시글 제목을 반환하는 메서드
    public String getTitle() {
        return title;
    }

    // 게시글 카테고리를 반환하는 메서드
    public String getCategory() {
        return category;
    }

    // 게시글 내용을 반환하는 메서드
    public String getContent() {
        return content;
    }

    // 게시글 위치를 반환하는 메서드
    public String getLocation() {
        return location;
    }

    // 조회수를 반환하는 메서드
    public int getViews() {
        return views;
    }

    // 게시글 작성 시간을 반환하는 메서드
    public String getCreatedAt() {
        return createdAt;
    }

    // 작성자 이름을 반환하는 메서드
    public String getUserName() {
        return userName;
    }
}
