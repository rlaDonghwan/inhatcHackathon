package com.example.hackathonproject.Education;

import com.example.hackathonproject.db.EducationDAO;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

public class EducationManager {
    private EducationDAO educationDAO;  // 데이터베이스 접근 객체

    // 생성자: EducationDAO 객체를 받아 초기화
    public EducationManager(EducationDAO educationDAO) {
        this.educationDAO = educationDAO;
    }

    // 새로운 교육 게시글을 추가하는 메서드
    public boolean addEducationPost(String title, String category, String content, String location, int fee, int userId) {
        // 현재 한국 표준시(KST) 시간을 가져옴
        ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return educationDAO.insertEducationPost(title, category, content, location, fee, userId, kstTime);
    }

    // 모든 교육 게시글을 가져오는 메서드
    public List<EducationPost> getAllEducationPosts() {
        return educationDAO.getAllEducationPosts();
    }
}
