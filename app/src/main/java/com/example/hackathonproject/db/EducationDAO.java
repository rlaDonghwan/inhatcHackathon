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
    public boolean insertEducationPost(String title, String category, String content, String location, int fee, int userId, ZonedDateTime kstTime) {
        String sql = "INSERT INTO Education (UserID, Title, Category, Content, Location, Fee, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, category);
            pstmt.setString(4, content);
            pstmt.setString(5, location);
            pstmt.setInt(6, fee); // 금액 설정

            // ZonedDateTime을 포맷팅된 문자열로 변환하여 저장
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(7, formattedDateTime); // DATETIME 형식으로 저장

            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            return rowsAffected > 0; // 삽입 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "게시글 삽입 실패", e); // 오류 로그 출력
            return false; // 삽입 실패 시 false 반환
        }
    }

    // 교육 게시글을 업데이트하는 메서드
    public boolean updateEducationPost(int educationId, String title, String category, String content, String location, int fee, int userId) {
        String sql = "UPDATE Education SET Title = ?, Category = ?, Content = ?, Location = ?, Fee = ? WHERE EducationID = ? AND UserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, category);
            pstmt.setString(3, content);
            pstmt.setString(4, location);
            pstmt.setInt(5, fee); // 금액 설정
            pstmt.setInt(6, educationId);
            pstmt.setInt(7, userId);

            int rowsAffected = pstmt.executeUpdate(); // 게시글 업데이트 쿼리 실행
            return rowsAffected > 0; // 업데이트 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "게시글 업데이트 실패", e); // 오류 로그 출력
            return false; // 업데이트 실패 시 false 반환
        }
    }

    // 특정 ID의 교육 게시글을 가져오는 메서드
    public EducationPost getEducationPostById(int educationId) {
        String sql = "SELECT EducationID, Title, Category, Content, Location, Fee, Views, CreatedAt, CompletedAt, VolunteerHoursEarned, u.Name, u.Role, e.UserID FROM Education e JOIN User u ON e.UserID = u.UserID WHERE EducationID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("EducationID");
                    String title = rs.getString("Title");
                    String category = rs.getString("Category");
                    String content = rs.getString("Content");
                    String location = rs.getString("Location");
                    int fee = rs.getInt("Fee");
                    int views = rs.getInt("Views");
                    String createdAt = rs.getString("CreatedAt");
                    String completedAt = rs.getString("CompletedAt");
                    int volunteerHoursEarned = rs.getInt("VolunteerHoursEarned");
                    String userName = rs.getString("Name");
                    String role = rs.getString("Role");
                    int userId = rs.getInt("UserID");

                    boolean isInstitution = "기관".equals(role); // '기관' 역할인지 확인

                    return new EducationPost(id, title, category, content, location, fee, views, createdAt, completedAt, volunteerHoursEarned, userName, userId, isInstitution);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "ID로 게시글 불러오기 실패", e); // 오류 로그 출력
        }
        return null; // 게시글이 없을 경우 null 반환
    }

    // 게시글 조회수를 증가시키는 메서드
    public void incrementPostViews(int educationId) {
        String sql = "UPDATE Education SET Views = Views + 1 WHERE EducationID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educationId);
            pstmt.executeUpdate(); // 조회수 증가 쿼리 실행
        } catch (SQLException e) {
            Log.e(TAG, "게시글 조회수 업데이트 실패", e); // 오류 로그 출력
        }
    }

    // 교육 게시글을 삭제하는 메서드
    public boolean deleteEducationPost(int educationId) {
        String sql = "DELETE FROM Education WHERE EducationID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educationId);
            int rowsAffected = pstmt.executeUpdate(); // 게시글 삭제 쿼리 실행
            return rowsAffected > 0; // 삭제 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "게시글 삭제 실패", e); // 오류 로그 출력
            return false; // 삭제 실패 시 false 반환
        }
    }

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
            Log.e(TAG, "사용자 이름 불러오기 실패", e); // 오류 로그 출력
        }
        return null; // 사용자 이름이 없으면 null 반환
    }

    // 모든 교육 게시글을 가져오는 메서드
    public List<EducationPost> getAllEducationPosts() {
        List<EducationPost> postList = new ArrayList<>();
        String sql = "SELECT e.EducationID, e.Title, e.Category, e.Content, e.Location, e.Fee, e.Views, e.CreatedAt, e.CompletedAt, e.VolunteerHoursEarned, u.Name, u.Role, e.UserID " +
                "FROM Education e JOIN User u ON e.UserID = u.UserID";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int educationId = rs.getInt("EducationID");
                String title = rs.getString("Title");
                String category = rs.getString("Category");
                String content = rs.getString("Content");
                String location = rs.getString("Location");
                int fee = rs.getInt("Fee");
                int views = rs.getInt("Views");
                String createdAt = rs.getString("CreatedAt");
                String completedAt = rs.getString("CompletedAt");
                int volunteerHoursEarned = rs.getInt("VolunteerHoursEarned");
                String userName = rs.getString("Name");
                String role = rs.getString("Role");
                int userId = rs.getInt("UserID");

                boolean isInstitution = "기관".equals(role); // 역할이 '기관'인 경우 true 설정

                postList.add(new EducationPost(educationId, title, category, content, location, fee, views, createdAt, completedAt, volunteerHoursEarned, userName, userId, isInstitution));
            }
        } catch (SQLException e) {
            Log.e(TAG, "게시글 불러오기 실패", e);
        }
        return postList;
    }
}
