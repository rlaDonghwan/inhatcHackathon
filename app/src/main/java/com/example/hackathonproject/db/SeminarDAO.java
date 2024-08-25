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
    private static final String TAG = "SeminarDAO";
    private DatabaseConnection dbConnection = new DatabaseConnection();

    // 강연 게시글을 삽입하는 메서드
    public boolean insertSeminarPost(int userId, String title, String content, String location, double fee, ZonedDateTime kstTime) {
        String sql = "INSERT INTO Lecture (UserID, Title, Content, Location, Fee, CreatedAt, Views) VALUES (?, ?, ?, ?, ?, ?, 0)";

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
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 모든 강연 게시글을 가져오는 메서드
    public List<SeminarPost> getAllSeminarPosts() {
        List<SeminarPost> postList = new ArrayList<>();
        String sql = "SELECT LectureID, l.UserID, u.Name, Title, Content, Location, Fee, Views, CreatedAt, CompletedAt " +
                "FROM Lecture l JOIN User u ON l.UserID = u.UserID";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int lectureId = rs.getInt("LectureID");
                int userId = rs.getInt("UserID");
                String userName = rs.getString("Name");  // 작성자 이름 가져오기
                String title = rs.getString("Title");
                String content = rs.getString("Content");
                String location = rs.getString("Location");
                double fee = rs.getDouble("Fee");
                int views = rs.getInt("Views");
                String createdAt = rs.getString("CreatedAt");
                String completedAt = rs.getString("CompletedAt");

                postList.add(new SeminarPost(lectureId, userId, userName, title, content, location, createdAt, completedAt, fee, views));
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load posts", e);
        }
        return postList;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 특정 ID의 강연 게시글을 가져오는 메서드
    public SeminarPost getSeminarPostById(int lectureId) {
        String sql = "SELECT LectureID, l.UserID, u.Name, Title, Content, Location, Fee, Views, CreatedAt, CompletedAt " +
                "FROM Lecture l JOIN User u ON l.UserID = u.UserID WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("UserID");
                    String userName = rs.getString("Name");  // 작성자 이름 가져오기
                    String title = rs.getString("Title");
                    String content = rs.getString("Content");
                    String location = rs.getString("Location");
                    double fee = rs.getDouble("Fee");
                    int views = rs.getInt("Views");
                    String createdAt = rs.getString("CreatedAt");
                    String completedAt = rs.getString("CompletedAt");

                    return new SeminarPost(lectureId, userId, userName, title, content, location, createdAt, completedAt, fee, views);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load lecture by ID", e);
        }
        return null;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 강연 게시글의 조회수를 증가시키는 메서드
    public void incrementSeminarPostViews(int lectureId) {
        String sql = "UPDATE Lecture SET Views = Views + 1 WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update lecture views", e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 강연 게시글을 삭제하는 메서드
    public boolean deleteSeminarPost(int lectureId) {
        String sql = "DELETE FROM Lecture WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete lecture", e);
            return false;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 강연 게시글을 업데이트하는 메서드
    public boolean updateSeminarPost(int lectureId, String title, String content, String location, double fee, int userId) {
        String sql = "UPDATE Lecture SET Title = ?, Content = ?, Location = ?, Fee = ? WHERE LectureID = ? AND UserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, location);
            pstmt.setDouble(4, fee);
            pstmt.setInt(5, lectureId);
            pstmt.setInt(6, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update lecture", e);
            return false;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
