package com.example.hackathonproject.Seminar;
public class SeminarPost {
    private int lectureId; // 강연 고유 ID
    private int userId; // 사용자 ID
    private String userName; // 사용자 이름
    private String title; // 강연 제목
    private String content; // 강연 내용
    private String location; // 강연 위치
    private String createdAt; // 강연 등록 시간
    private String completedAt; // 강연 완료 시간
    private double fee; // 강연료
    private int views; // 조회수

    // 생성자
    public SeminarPost(int lectureId, int userId, String userName, String title, String content, String location, String createdAt, String completedAt, double fee, int views) {
        this.lectureId = lectureId;
        this.userId = userId;
        this.userName = userName; // 작성자 이름
        this.title = title;
        this.content = content;
        this.location = location;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.fee = fee;
        this.views = views;
    }

    // getter 메서드들
    public int getLectureId() { return lectureId; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; } // 추가
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLocation() { return location; }
    public String getCreatedAt() { return createdAt; }
    public String getCompletedAt() { return completedAt; }
    public double getFee() { return fee; }
    public int getViews() { return views; }

    // setter 메서드들
    public void setLectureId(int lectureId) { this.lectureId = lectureId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; } // 추가
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLocation(String location) { this.location = location; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public void setFee(double fee) { this.fee = fee; }
    public void setViews(int views) { this.views = views; }
}

