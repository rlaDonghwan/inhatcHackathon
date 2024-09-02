package com.example.hackathonproject.db;

import android.util.Log;
import com.example.hackathonproject.Lecture.LecturePost;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LectureDAO {
    private static final String TAG = "LectureDAO";
    private DatabaseConnection dbConnection = DatabaseConnection.getInstance(); // Singleton 인스턴스를 가져옴

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
        String sql = "SELECT Lecture.LectureID, Lecture.UserID, User.Name, Lecture.Title, Lecture.Content, Lecture.Location, Lecture.CreatedAt, Lecture.Fee, Lecture.Views, Lecture.isYouthAudienceAllowed, LectureImage.ImageData, User.ProfileImagePath " +
                "FROM Lecture " +
                "LEFT JOIN User ON Lecture.UserID = User.UserID " +  // User 테이블과 조인하여 작성자 이름 및 프로필 이미지 가져오기
                "LEFT JOIN LectureImage ON Lecture.LectureID = LectureImage.LectureID"; // LectureImage 테이블과 조인하여 이미지 데이터 가져오기

        List<LecturePost> posts = new ArrayList<>();

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
                String createdAt = rs.getString("CreatedAt");
                double fee = rs.getDouble("Fee");
                int views = rs.getInt("Views");
                boolean isYouthAudienceAllowed = rs.getBoolean("isYouthAudienceAllowed");
                byte[] imageData = rs.getBytes("ImageData");  // 강연 이미지 데이터 가져오기
                byte[] profileImageData = rs.getBytes("ProfileImagePath");  // 프로필 이미지 데이터 가져오기

                // LecturePost 객체 생성
                LecturePost post = new LecturePost(lectureId, userId, userName, title, content, location, createdAt, null, fee, views, isYouthAudienceAllowed, imageData, profileImageData);
                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    // 특정 ID의 강연 게시글을 가져오는 메서드
    public LecturePost getLecturePostById(int lectureId) {
        String sql = "SELECT l.LectureID, l.UserID, u.Name, l.Title, l.Content, l.Location, l.Fee, l.Views, l.CreatedAt, l.IsYouthAudienceAllowed, li.ImageData, u.ProfileImagePath " +
                "FROM Lecture l " +
                "JOIN User u ON l.UserID = u.UserID " +
                "LEFT JOIN LectureImage li ON l.LectureID = li.LectureID " +  // LectureImage 테이블과 조인하여 이미지 데이터를 가져옴
                "WHERE l.LectureID = ?";

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
                    boolean isYouthAudienceAllowed = rs.getBoolean("IsYouthAudienceAllowed");
                    byte[] imageData = rs.getBytes("ImageData");  // 강연 이미지 데이터 가져오기
                    byte[] profileImageData = rs.getBytes("ProfileImagePath");  // 프로필 이미지 데이터 가져오기

                    return new LecturePost(lectureId, userId, userName, title, content, location, createdAt, null, fee, views, isYouthAudienceAllowed, imageData, profileImageData);
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

    // 강연 게시글과 이미지를 함께 삽입하는 메서드
    public boolean submitLectureWithImage(String title, String content, String location, double fee, int userId, ZonedDateTime kstTime, boolean isYouthAudienceAllowed, byte[] imageData) {
        String insertLectureSql = "INSERT INTO Lecture (UserID, Title, Content, Location, Fee, CreatedAt, isYouthAudienceAllowed) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertImageSql = "INSERT INTO LectureImage (LectureID, ImageData) VALUES (?, ?)";

        try (Connection conn = dbConnection.connect()) {
            conn.setAutoCommit(false);  // 트랜잭션 시작

            int lectureId;
            // 1. 강연 게시글 삽입
            try (PreparedStatement pstmt = conn.prepareStatement(insertLectureSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, title);
                pstmt.setString(3, content);
                pstmt.setString(4, location);
                pstmt.setDouble(5, fee);
                pstmt.setString(6, kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                pstmt.setBoolean(7, isYouthAudienceAllowed);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lectureId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. 이미지 삽입
            if (imageData != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertImageSql)) {
                    pstmt.setInt(1, lectureId);
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

    public boolean updateLectureWithImage(int lectureId, String title, String content, String location, double fee, int userId, boolean isYouthAudienceAllowed, byte[] imageData) {
        String updateLectureSql = "UPDATE Lecture SET Title = ?, Content = ?, Location = ?, Fee = ?, IsYouthAudienceAllowed = ? WHERE LectureID = ? AND UserID = ?";
        String updateImageSql = "UPDATE LectureImage SET ImageData = ? WHERE LectureID = ?";
        String insertImageSql = "INSERT INTO LectureImage (LectureID, ImageData) VALUES (?, ?)";

        try (Connection conn = dbConnection.connect()) {
            conn.setAutoCommit(false);  // 트랜잭션 시작

            // 강연 게시글 업데이트
            try (PreparedStatement pstmt = conn.prepareStatement(updateLectureSql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, content);
                pstmt.setString(3, location);
                pstmt.setDouble(4, fee);
                pstmt.setBoolean(5, isYouthAudienceAllowed);
                pstmt.setInt(6, lectureId);
                pstmt.setInt(7, userId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 이미지 업데이트 또는 삽입
            if (imageData != null) {
                int imageRowsAffected;
                try (PreparedStatement pstmt = conn.prepareStatement(updateImageSql)) {
                    pstmt.setBytes(1, imageData);
                    pstmt.setInt(2, lectureId);

                    imageRowsAffected = pstmt.executeUpdate();
                }

                // 이미지 업데이트가 이루어지지 않은 경우, 이미지 삽입
                if (imageRowsAffected == 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(insertImageSql)) {
                        pstmt.setInt(1, lectureId);
                        pstmt.setBytes(2, imageData);

                        int insertRowsAffected = pstmt.executeUpdate();
                        if (insertRowsAffected == 0) {
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }

            conn.commit();  // 트랜잭션 커밋
            return true;

        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 및 이미지 업데이트 중 오류", e);
            return false;
        }
    }


    public byte[] getUserProfileImage(int userId) {
        String sql = "SELECT ProfileImagePath FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("ProfileImagePath");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "프로필 이미지 가져오기 실패", e);
        }
        return null;
    }

}
