package com.example.hackathonproject.db;

import android.util.Log;
import com.example.hackathonproject.Seminar.SeminarPost;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SeminarDAO {
    private static final String TAG = "SeminarDAO"; // 로그 태그
    private DatabaseConnection dbConnection = new DatabaseConnection(); // 데이터베이스 연결 객체

    // 강연 게시글을 삽입하는 메서드
    public boolean insertLecture(String title, String content, String location, double fee, int userId, ZonedDateTime kstTime) {
        String sql = "INSERT INTO Lecture (UserID, Title, Content, Location, Fee, CreatedAt, Views) VALUES (?, ?, ?, ?, ?, ?, 0)";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, location);
            pstmt.setDouble(5, fee);

            // ZonedDateTime을 포맷팅된 문자열로 변환하여 저장
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(6, formattedDateTime); // DATETIME 형식으로 저장

            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            return rowsAffected > 0; // 삽입 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert lecture", e); // 오류 로그 출력
            return false; // 삽입 실패 시 false 반환
        }
    }

    // 모든 강연 게시글을 가져오는 메서드
    public List<SeminarPost> getAllLectures() {
        List<SeminarPost> postList = new ArrayList<>(); // 게시글 목록을 담을 리스트
        String sql = "SELECT l.LectureID, l.UserID, l.Title, l.Content, l.Location, l.Fee, l.CreatedAt, l.CompletedAt, l.Views " +
                "FROM Lecture l JOIN User u ON l.UserID = u.UserID"; // SQL 쿼리

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int lectureId = rs.getInt("LectureID");
                int userId = rs.getInt("UserID");
                String title = rs.getString("Title");
                String content = rs.getString("Content");
                String location = rs.getString("Location");
                double fee = rs.getDouble("Fee");
                String createdAt = rs.getString("CreatedAt"); // KST로 저장된 시간을 그대로 가져옴
                String completedAt = rs.getString("CompletedAt");
                int views = rs.getInt("Views");

                postList.add(new SeminarPost(lectureId, userId, title, content, location, createdAt, completedAt, fee, views)); // 리스트에 게시글 추가
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load lectures", e); // 오류 로그 출력
        }
        return postList; // 게시글 리스트 반환
    }

    // 특정 ID의 강연 게시글을 가져오는 메서드
    public SeminarPost getLectureById(int lectureId) {
        String sql = "SELECT LectureID, UserID, Title, Content, Location, Fee, CreatedAt, CompletedAt, Views " +
                "FROM Lecture WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("UserID");
                    String title = rs.getString("Title");
                    String content = rs.getString("Content");
                    String location = rs.getString("Location");
                    double fee = rs.getDouble("Fee");
                    String createdAt = rs.getString("CreatedAt"); // KST로 저장된 시간을 그대로 가져옴
                    String completedAt = rs.getString("CompletedAt");
                    int views = rs.getInt("Views");

                    return new SeminarPost(lectureId, userId, title, content, location, createdAt, completedAt, fee, views); // 게시글 객체 생성 후 반환
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load lecture by ID", e); // 오류 로그 출력
        }
        return null; // 게시글이 없으면 null 반환
    }

    // 강연 게시글의 조회수를 증가시키는 메서드
    public void incrementLectureViews(int lectureId) {
        String sql = "UPDATE Lecture SET Views = Views + 1 WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            pstmt.executeUpdate(); // 조회수 증가 쿼리 실행
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update lecture views", e); // 오류 로그 출력
        }
    }

    // 강연 게시글을 삭제하는 메서드
    public boolean deleteLecture(int lectureId) {
        String sql = "DELETE FROM Lecture WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            int rowsAffected = pstmt.executeUpdate(); // 게시글 삭제 쿼리 실행
            return rowsAffected > 0; // 삭제 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete lecture", e); // 오류 로그 출력
            return false; // 삭제 실패 시 false 반환
        }
    }

    // 강연 게시글을 업데이트하는 메서드
    public boolean updateLecture(int lectureId, String title, String content, String location, double fee, int userId) {
        String sql = "UPDATE Lecture SET Title = ?, Content = ?, Location = ?, Fee = ? WHERE LectureID = ? AND UserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, location);
            pstmt.setDouble(4, fee);
            pstmt.setInt(5, lectureId);
            pstmt.setInt(6, userId);

            int rowsAffected = pstmt.executeUpdate(); // 게시글 업데이트 쿼리 실행
            return rowsAffected > 0; // 업데이트 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update lecture", e); // 오류 로그 출력
            return false; // 업데이트 실패 시 false 반환
        }
    }

    public boolean insertSeminarPost(int userId, String title, String content, String location, double fee, ZonedDateTime kstTime) {
        String sql = "INSERT INTO Lecture (UserID, Title, Content, Location, Fee, CreatedAt) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, location);
            pstmt.setDouble(5, fee);
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(6, formattedDateTime);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert post", e);
            return false;
        }
    }

    public List<SeminarPost> getAllSeminarPosts() {
        List<SeminarPost> postList = new ArrayList<>();
        String sql = "SELECT LectureID, UserID, Title, Content, Location, Views, Fee, CreatedAt, CompletedAt FROM Lecture";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int lectureId = rs.getInt("LectureID");
                int userId = rs.getInt("UserID");
                String title = rs.getString("Title");
                String content = rs.getString("Content");
                String location = rs.getString("Location");
                int views = rs.getInt("Views");
                double fee = rs.getDouble("Fee");
                String createdAt = rs.getString("CreatedAt");
                String completedAt = rs.getString("CompletedAt");

                postList.add(new SeminarPost(lectureId, userId, title, content, location, createdAt, completedAt, fee, views));
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load posts", e);
        }
        return postList;
    }
}
