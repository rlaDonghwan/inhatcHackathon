package com.example.hackathonproject.db;

import android.util.Log;

import com.example.hackathonproject.Education.EducationPost;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EducationDAO {
    private static final String TAG = "EducationDAO"; // 로그 태그
    private DatabaseConnection dbConnection = new DatabaseConnection(); // 데이터베이스 연결 객체

    // 교육 게시글을 삽입하는 메서드
    public boolean insertEducationPost(String title, String category, String content, String location, int userId, ZonedDateTime kstTime) {
        String sql = "INSERT INTO EducationPost (UserID, Title, Category, Content, Location, CreatedAt) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, category);
            pstmt.setString(4, content);
            pstmt.setString(5, location);

            // ZonedDateTime을 포맷팅된 문자열로 변환하여 저장
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(6, formattedDateTime); // DATETIME 형식으로 저장

            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            return rowsAffected > 0; // 삽입 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert post", e); // 오류 로그 출력
            return false; // 삽입 실패 시 false 반환
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 모든 교육 게시글을 가져오는 메서드
    public List<EducationPost> getAllEducationPosts() {
        List<EducationPost> postList = new ArrayList<>(); // 게시글 목록을 담을 리스트
        String sql = "SELECT e.PostID, e.Title, e.Category, e.Content, e.Location, e.Views, e.CreatedAt, u.Name, e.UserID " +
                "FROM EducationPost e JOIN User u ON e.UserID = u.UserID"; // SQL 쿼리
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int postId = rs.getInt("PostID");
                String title = rs.getString("Title");
                String category = rs.getString("Category");
                String content = rs.getString("Content");
                String location = rs.getString("Location");
                int views = rs.getInt("Views");
                String createdAt = rs.getString("CreatedAt"); // KST로 저장된 시간을 그대로 가져옴
                String userName = rs.getString("Name");
                int userId = rs.getInt("UserID");  // userId 추가

                postList.add(new EducationPost(postId, title, category, content, location, views, createdAt, userName, userId)); // userId 추가
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load posts", e); // 오류 로그 출력
        }
        return postList; // 게시글 리스트 반환
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 특정 ID의 교육 게시글을 가져오는 메서드
    public EducationPost getEducationPostById(int postId) {
        String sql = "SELECT PostID, Title, Category, Content, Location, Views, CreatedAt, UserID FROM EducationPost WHERE PostID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("PostID");
                    String title = rs.getString("Title");
                    String category = rs.getString("Category");
                    String content = rs.getString("Content");
                    String location = rs.getString("Location");
                    int views = rs.getInt("Views");
                    String createdAt = rs.getString("CreatedAt");
                    int userId = rs.getInt("UserID");  // <---- 여기에 추가

                    String userName = getUserNameById(userId);
                    return new EducationPost(id, title, category, content, location, views, createdAt, userName, userId);

                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load post by ID", e);
        }
        return null;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 게시글 조회수를 증가시키는 메서드
    public void incrementPostViews(int postId) {
        String sql = "UPDATE EducationPost SET Views = Views + 1 WHERE PostID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.executeUpdate(); // 조회수 증가 쿼리 실행
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update post views", e); // 오류 로그 출력
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 교육 게시글을 삭제하는 메서드
    public boolean deleteEducationPost(int postId) {
        String sql = "DELETE FROM EducationPost WHERE PostID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            int rowsAffected = pstmt.executeUpdate(); // 게시글 삭제 쿼리 실행
            return rowsAffected > 0; // 삭제 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete post", e); // 오류 로그 출력
            return false; // 삭제 실패 시 false 반환
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자 ID로 사용자 이름을 가져오는 메서드
    private String getUserNameById(int userId) {
        String sql = "SELECT Name FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name"); // 사용자 이름 반환
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load user name by ID", e); // 오류 로그 출력
        }
        return null; // 사용자 이름이 없으면 null 반환
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    // 교육 게시글을 업데이트하는 메서드
    public boolean updateEducationPost(int postId, String title, String category, String content, String location, int userId) {
        String sql = "UPDATE EducationPost SET Title = ?, Category = ?, Content = ?, Location = ? WHERE PostID = ? AND UserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, category);
            pstmt.setString(3, content);
            pstmt.setString(4, location);
            pstmt.setInt(5, postId);
            pstmt.setInt(6, userId);

            int rowsAffected = pstmt.executeUpdate(); // 게시글 업데이트 쿼리 실행
            return rowsAffected > 0; // 업데이트 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update post", e); // 오류 로그 출력
            return false; // 업데이트 실패 시 false 반환
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
