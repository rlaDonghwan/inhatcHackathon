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
    private int userId;  // 작성자 ID  <---- 여기에 추가

    // 생성자: 모든 필드를 초기화
    public EducationPost(int postId, String title, String category, String content, String location, int views, String createdAt, String userName, int userId) {
        this.postId = postId;
        this.title = title;
        this.category = category;
        this.content = content;
        this.location = location;
        this.views = views;  // 조회수 초기화
        this.createdAt = createdAt;
        this.userName = userName;
        this.userId = userId;  // <---- 여기에 추가
    }

    // getter 메서드: 각 필드에 접근할 수 있는 메서드들

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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserId() {  // <---- 여기에 추가
        return userId;
    }
}

