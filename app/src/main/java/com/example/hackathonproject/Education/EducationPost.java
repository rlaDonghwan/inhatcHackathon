package com.example.hackathonproject.Education;

public class EducationPost {
    private int educationId;
    private String title;
    private String category;
    private String content;
    private String location;
    private int fee;
    private int views;
    private String createdAt;
    private String completedAt;
    private int volunteerHoursEarned;
    private String userName;
    private int userId;
    private boolean isInstitution;  // 기관 사용자 여부

    // 생성자
    public EducationPost(int educationId, String title, String category, String content, String location, int fee, int views, String createdAt, String completedAt, int volunteerHoursEarned, String userName, int userId, boolean isInstitution) {
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
        this.isInstitution = isInstitution;  // 기관 여부 설정
    }

    public boolean isInstitution() {
        return isInstitution;
    }
    // getter 메서드: 각 필드에 접근할 수 있는 메서드들

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
}
