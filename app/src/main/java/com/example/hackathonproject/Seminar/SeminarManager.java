package com.example.hackathonproject.Seminar;

import com.example.hackathonproject.db.SeminarDAO;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class SeminarManager {
    private SeminarDAO seminarDAO;

    public SeminarManager(SeminarDAO seminarDAO) {
        this.seminarDAO = seminarDAO;
    }

    public boolean addSeminarPost(int userId, String title, String content, String location, double fee) {
        ZonedDateTime kstTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return seminarDAO.insertSeminarPost(userId, title, content, location, fee, kstTime);
    }

    public List<SeminarPost> getAllSeminarPosts() {
        return seminarDAO.getAllSeminarPosts();
    }
}
