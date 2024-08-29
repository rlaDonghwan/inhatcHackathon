package com.example.hackathonproject.Lecture;

import com.example.hackathonproject.db.LectureDAO;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class LectureManager {
    private LectureDAO lectureDAO; // LectureDAO 객체를 저장할 변수

    // 생성자: LectureDAO 객체를 초기화
    public LectureManager(LectureDAO lectureDAO) {
        this.lectureDAO = lectureDAO;
    }

    // 강연 게시글을 추가하는 메서드
    public boolean addLecturePost(int userId, String title, String content, String location, double fee, boolean isYouthAudienceAllowed) {
        // 현재 한국 시간(KST)을 가져옴
        ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        // 강연 게시글을 데이터베이스에 삽입
        return lectureDAO.insertLecturePost(userId, title, content, location, fee, kstTime, isYouthAudienceAllowed);
    }

    // 모든 강연 게시글을 가져오는 메서드
    public List<LecturePost> getAllLecturePosts() {
        // 강연 게시글 목록을 데이터베이스에서 가져옴
        return lectureDAO.getAllLecturePosts();
    }
}
