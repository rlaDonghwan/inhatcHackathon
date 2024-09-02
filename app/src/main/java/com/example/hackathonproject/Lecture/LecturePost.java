package com.example.hackathonproject.Lecture;

public class LecturePost {
    private int lectureId; // 강연 고유 ID
    private int userId; // 사용자 ID
    private String userName; // 사용자 이름 (강연 작성자)
    private String title; // 강연 제목
    private String content; // 강연 내용
    private String location; // 강연 위치
    private String createdAt; // 강연 등록 시간
    private String completedAt; // 강연 완료 시간
    private double fee; // 강연료
    private int views; // 조회수
    private boolean isYouthAudienceAllowed; // 청년 참관 가능 여부
    private byte[] imageData;  // 이미지 데이터를 저장할 필드
    private byte[] profileImageData;  // 프로필 이미지 데이터를 저장할 필드

    // 생성자
    public LecturePost(int lectureId, int userId, String userName, String title, String content, String location,
                       String createdAt, String completedAt, double fee, int views,
                       boolean isYouthAudienceAllowed, byte[] imageData, byte[] profileImageData) {
        this.lectureId = lectureId;
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.location = location;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.fee = fee;
        this.views = views;
        this.isYouthAudienceAllowed = isYouthAudienceAllowed;
        this.imageData = imageData;
        this.profileImageData = profileImageData;
    }


    // getter 메서드들
    public int getLectureId() { return lectureId; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLocation() { return location; }
    public String getCreatedAt() { return createdAt; }
    public String getCompletedAt() { return completedAt; }
    public double getFee() { return fee; }
    public int getViews() { return views; }
    public boolean isYouthAudienceAllowed() { return isYouthAudienceAllowed; }
    public byte[] getImageData() { return imageData; }
    public byte[] getProfileImageData() { return profileImageData; }

    // setter 메서드들
    public void setLectureId(int lectureId) { this.lectureId = lectureId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLocation(String location) { this.location = location; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public void setFee(double fee) { this.fee = fee; }
    public void setViews(int views) { this.views = views; }
    public void setYouthAudienceAllowed(boolean youthAudienceAllowed) { this.isYouthAudienceAllowed = youthAudienceAllowed; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public void setProfileImageData(byte[] profileImageData) { this.profileImageData = profileImageData; }
}
