package com.example.hackathonproject.Education;

public class EducationPost {
    private int educationId;  // 교육 게시글 ID
    private String title;  // 게시글 제목
    private String category;  // 게시글 카테고리
    private String content;  // 게시글 내용
    private String location;  // 게시글 위치
    private int fee;  // 교육료
    private int views;  // 조회수 필드
    private String createdAt;  // 게시글 작성 시간
    private String completedAt;  // 게시글 완료 시간
    private int volunteerHoursEarned;  // 획득한 봉사 시간
    private String userName;  // 작성자 이름
    private int userId;  // 작성자 ID
    private byte[] imageData; // 이미지 데이터를 위한 필드
    private boolean isInstitution;  // 기관 사용자 여부
    private String role;  // Role 필드 추가 (기관, 학교, 청년 등)

    // 생성자
    public EducationPost(int educationId, String title, String category, String content, String location, int fee, int views, String createdAt, String completedAt, int volunteerHoursEarned, String userName, int userId, boolean isInstitution, byte[] imageData, String role) {
        this.educationId = educationId;
        this.title = title;
        this.category = category;
        this.content = content;
        this.location = location;
        this.fee = fee;
        this.views = views;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.volunteerHoursEarned = volunteerHoursEarned;
        this.userName = userName;
        this.userId = userId;
        this.isInstitution = isInstitution;
        this.imageData = imageData; // 이미지 데이터 초기화
        this.role = role; // role 초기화
    }

    // 기존 메서드들...

    public boolean isInstitution() {
        return isInstitution;
    }

    public void setInstitution(boolean institution) {
        isInstitution = institution;
    }

    public int getEducationId() {
        return educationId;
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

    public int getFee() {
        return fee;
    }

    public int getViews() {
        return views;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public int getVolunteerHoursEarned() {
        return volunteerHoursEarned;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserId() {
        return userId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    // Role 관련 메서드
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isInstitutionOrSchool() {
        return "기관".equals(role) || "학교".equals(role);
    }
}