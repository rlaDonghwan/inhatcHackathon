package com.example.hackathonproject.Seminar;

import com.example.hackathonproject.db.SeminarDAO;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class SeminarManager {
    private SeminarDAO seminarDAO; // SeminarDAO 객체를 저장할 변수

    // 생성자: SeminarDAO 객체를 초기화
    public SeminarManager(SeminarDAO seminarDAO) {
        this.seminarDAO = seminarDAO;
    }

    // 세미나 게시글을 추가하는 메서드
    public boolean addSeminarPost(int userId, String title, String content, String location, double fee) {
        // 현재 한국 시간(KST)을 가져옴
        ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        // 세미나 게시글을 데이터베이스에 삽입
        return seminarDAO.insertSeminarPost(userId, title, content, location, fee, kstTime);
    }

    // 모든 세미나 게시글을 가져오는 메서드
    public List<SeminarPost> getAllSeminarPosts() {
        // 세미나 게시글 목록을 데이터베이스에서 가져옴
        return seminarDAO.getAllSeminarPosts();
    }
}
