package com.example.hackathonproject.db;

import android.util.Log;
import com.example.hackathonproject.Lecture.LecturePost;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LectureDAO {
    private static final String TAG = "LectureDAO";
    private DatabaseConnection dbConnection = new DatabaseConnection();

    // 강연 게시글을 삽입하는 메서드
    public boolean insertLecturePost(int userId, String title, String content, String location, double fee, ZonedDateTime kstTime, boolean isYouthAudienceAllowed) {
        String sql = "INSERT INTO Lecture (UserID, Title, Content, Location, Fee, CreatedAt, Views, IsYouthAudienceAllowed) VALUES (?, ?, ?, ?, ?, ?, 0, ?)";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // SQL 쿼리 매개변수 설정
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, location);
            pstmt.setDouble(5, fee);

            // ZonedDateTime을 포맷팅된 문자열로 변환하여 저장
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(6, formattedDateTime);
            pstmt.setBoolean(7, isYouthAudienceAllowed);

            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            return rowsAffected > 0; // 삽입 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 삽입 실패", e);
            return false;
        }
    }

    // 모든 강연 게시글을 가져오는 메서드
    public List<LecturePost> getAllLecturePosts() {
        List<LecturePost> postList = new ArrayList<>();
        String sql = "SELECT LectureID, l.UserID, u.Name, Title, Content, Location, Fee, Views, CreatedAt, CompletedAt, IsYouthAudienceAllowed " +
                "FROM Lecture l JOIN User u ON l.UserID = u.UserID";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int lectureId = rs.getInt("LectureID");
                int userId = rs.getInt("UserID");
                String userName = rs.getString("Name");
                String title = rs.getString("Title");
                String content = rs.getString("Content");
                String location = rs.getString("Location");
                double fee = rs.getDouble("Fee");
                int views = rs.getInt("Views");
                String createdAt = rs.getString("CreatedAt");
                String completedAt = rs.getString("CompletedAt");
                boolean isYouthAudienceAllowed = rs.getBoolean("IsYouthAudienceAllowed");

                postList.add(new LecturePost(lectureId, userId, userName, title, content, location, createdAt, completedAt, fee, views, isYouthAudienceAllowed));
            }
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 불러오기 실패", e);
        }
        return postList;
    }

    // 특정 ID의 강연 게시글을 가져오는 메서드
    public LecturePost getLecturePostById(int lectureId) {
        String sql = "SELECT LectureID, l.UserID, u.Name, Title, Content, Location, Fee, Views, CreatedAt, CompletedAt, IsYouthAudienceAllowed " +
                "FROM Lecture l JOIN User u ON l.UserID = u.UserID WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("UserID");
                    String userName = rs.getString("Name");
                    String title = rs.getString("Title");
                    String content = rs.getString("Content");
                    String location = rs.getString("Location");
                    double fee = rs.getDouble("Fee");
                    int views = rs.getInt("Views");
                    String createdAt = rs.getString("CreatedAt");
                    String completedAt = rs.getString("CompletedAt");
                    boolean isYouthAudienceAllowed = rs.getBoolean("IsYouthAudienceAllowed");

                    return new LecturePost(lectureId, userId, userName, title, content, location, createdAt, completedAt, fee, views, isYouthAudienceAllowed);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "ID로 강연 게시글 불러오기 실패", e);
        }
        return null;
    }

    // 강연 게시글의 조회수를 증가시키는 메서드
    public void incrementLecturePostViews(int lectureId) {
        String sql = "UPDATE Lecture SET Views = Views + 1 WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 조회수 업데이트 실패", e);
        }
    }

    // 강연 게시글을 삭제하는 메서드
    public boolean deleteLecturePost(int lectureId) {
        String sql = "DELETE FROM Lecture WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 삭제 실패", e);
            return false;
        }
    }

    // 강연 게시글을 업데이트하는 메서드
    public boolean updateLecturePost(int lectureId, String title, String content, String location, double fee, int userId, boolean isYouthAudienceAllowed) {
        String sql = "UPDATE Lecture SET Title = ?, Content = ?, Location = ?, Fee = ?, IsYouthAudienceAllowed = ? WHERE LectureID = ? AND UserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, location);
            pstmt.setDouble(4, fee);
            pstmt.setBoolean(5, isYouthAudienceAllowed);
            pstmt.setInt(6, lectureId);
            pstmt.setInt(7, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 업데이트 실패", e);
            return false;
        }
    }
}
