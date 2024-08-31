package com.example.hackathonproject.db;

import android.util.Log;
import com.example.hackathonproject.Education.EducationPost;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
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
    //-----------------------------------------------------------------------------------------------------------------------------------------------

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
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public EducationPost getEducationPostById(int educationId) {
        String sql = "SELECT e.EducationID, e.Title, e.Category, e.Content, e.Location, e.Fee, e.Views, e.CreatedAt, " +
                "e.VolunteerHoursEarned, e.UserID, u.Name, u.Role, i.ImageData " +
                "FROM Education e " +
                "JOIN User u ON e.UserID = u.UserID " +
                "LEFT JOIN EducationImage i ON e.EducationID = i.EducationID " +
                "WHERE e.EducationID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 교육 게시글 정보 가져오기
                    int id = rs.getInt("EducationID");
                    String title = rs.getString("Title");
                    String category = rs.getString("Category");
                    String content = rs.getString("Content");
                    String location = rs.getString("Location");
                    int fee = rs.getInt("Fee");
                    int views = rs.getInt("Views");
                    String createdAt = rs.getString("CreatedAt");
                    int volunteerHoursEarned = rs.getInt("VolunteerHoursEarned");
                    int userId = rs.getInt("UserID");
                    String userName = rs.getString("Name");
                    String role = rs.getString("Role");
                    byte[] imageData = rs.getBytes("ImageData");  // 이미지 데이터 가져오기

                    boolean isInstitution = "기관".equals(role);

                    // 새로운 생성자 사용
                    return new EducationPost(id, title, category, content, location, fee, views, createdAt, null, volunteerHoursEarned, userName, userId, isInstitution, imageData);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "ID로 게시글 불러오기 실패", e);
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
        // 교육 게시글과 관련된 사용자 정보 및 이미지 데이터를 가져오는 SQL 쿼리
        String sql = "SELECT e.EducationID, e.Title, e.Category, e.Content, e.Location, e.Fee, e.Views, e.CreatedAt, e.VolunteerHoursEarned, " +
                "u.Name, u.Role, e.UserID, i.ImageData " +
                "FROM Education e " +
                "JOIN User u ON e.UserID = u.UserID " +
                "LEFT JOIN EducationImage i ON e.EducationID = i.EducationID"; // 이미지 데이터를 가져오기 위해 LEFT JOIN 사용

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // 결과 집합을 순회하며 각 게시글 객체를 생성하여 리스트에 추가
            while (rs.next()) {
                int educationId = rs.getInt("EducationID");
                String title = rs.getString("Title");
                String category = rs.getString("Category");
                String content = rs.getString("Content");
                String location = rs.getString("Location");
                int fee = rs.getInt("Fee");
                int views = rs.getInt("Views");
                String createdAt = rs.getString("CreatedAt");
                int volunteerHoursEarned = rs.getInt("VolunteerHoursEarned");
                String userName = rs.getString("Name");
                String role = rs.getString("Role");
                int userId = rs.getInt("UserID");
                byte[] imageData = rs.getBytes("ImageData");  // 이미지 데이터 가져오기

                boolean isInstitution = "기관".equals(role); // 역할이 '기관'인 경우 true 설정

                // 'EducationPost' 객체 생성: 수정된 생성자를 사용
                postList.add(new EducationPost(educationId, title, category, content, location, fee, views, createdAt, null, volunteerHoursEarned, userName, userId, isInstitution, imageData));
            }
        } catch (SQLException e) {
            Log.e(TAG, "게시글 불러오기 실패", e);
        }
        return postList;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public boolean submitEducationWithImage(String title, String category, String description, String location, int fee, int userId, ZonedDateTime kstTime, byte[] imageData) {
        String insertPostSql = "INSERT INTO Education (UserID, Title, Category, Content, Location, Fee, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertImageSql = "INSERT INTO EducationImage (EducationID, ImageData) VALUES (?, ?)";

        try (Connection conn = dbConnection.connect()) {
            conn.setAutoCommit(false);  // 트랜잭션 시작

            int postId;
            // 1. 게시글 삽입
            try (PreparedStatement pstmt = conn.prepareStatement(insertPostSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, title);
                pstmt.setString(3, category);
                pstmt.setString(4, description);
                pstmt.setString(5, location);
                pstmt.setInt(6, fee);
                pstmt.setString(7, kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        postId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. 이미지 삽입
            if (imageData != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertImageSql)) {
                    pstmt.setInt(1, postId);
                    pstmt.setBytes(2, imageData);

                    int imageRowsAffected = pstmt.executeUpdate();
                    if (imageRowsAffected == 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            conn.commit();  // 트랜잭션 커밋
            return true;

        } catch (SQLException e) {
            Log.e(TAG, "트랜잭션 처리 중 오류", e);
            return false;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public byte[] getEducationImage(int educationId) {
        String sql = "SELECT ImageData FROM EducationImage WHERE EducationID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, educationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("ImageData");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "이미지 불러오기 실패", e);
        }
        return null; // 이미지가 없으면 null 반환
    }

    public boolean updateEducationPostWithImage(int educationId, String title, String category, String content, String location, int fee, int userId, byte[] imageData) {
        String updatePostSql = "UPDATE Education SET Title = ?, Category = ?, Content = ?, Location = ?, Fee = ? WHERE EducationID = ? AND UserID = ?";
        String updateImageSql = "UPDATE EducationImage SET ImageData = ? WHERE EducationID = ?";

        try (Connection conn = dbConnection.connect()) {
            conn.setAutoCommit(false);  // 트랜잭션 시작

            // 게시글 업데이트
            try (PreparedStatement pstmt = conn.prepareStatement(updatePostSql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, category);
                pstmt.setString(3, content);
                pstmt.setString(4, location);
                pstmt.setInt(5, fee);
                pstmt.setInt(6, educationId);
                pstmt.setInt(7, userId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 이미지 업데이트
            if (imageData != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateImageSql)) {
                    pstmt.setBytes(1, imageData);
                    pstmt.setInt(2, educationId);

                    int imageRowsAffected = pstmt.executeUpdate();
                    if (imageRowsAffected == 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            conn.commit();  // 트랜잭션 커밋
            return true;

        } catch (SQLException e) {
            Log.e(TAG, "게시글 및 이미지 업데이트 중 오류", e);
            return false;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

}
