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

public class LectureDAO { // 클래스명 LectureDAO로 변경
    private static final String TAG = "LectureDAO"; // 로그 태그, 클래스명에 맞게 변경
    private DatabaseConnection dbConnection = new DatabaseConnection(); // 데이터베이스 연결 객체

    // 강연 게시글을 삽입하는 메서드
    public boolean insertLecturePost(int userId, String title, String content, String location, double fee, ZonedDateTime kstTime) {
        String sql = "INSERT INTO Lecture (UserID, Title, Content, Location, Fee, CreatedAt, Views) VALUES (?, ?, ?, ?, ?, ?, 0)";

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

            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            return rowsAffected > 0; // 삽입 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 삽입 실패", e); // 오류 로그 출력
            return false; // 삽입 실패 시 false 반환
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 모든 강연 게시글을 가져오는 메서드
    public List<LecturePost> getAllLecturePosts() { // 메서드명 수정
        List<LecturePost> postList = new ArrayList<>(); // 강연 게시글 리스트
        String sql = "SELECT LectureID, l.UserID, u.Name, Title, Content, Location, Fee, Views, CreatedAt, CompletedAt " +
                "FROM Lecture l JOIN User u ON l.UserID = u.UserID"; // SQL 쿼리

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // 결과 집합을 순회하며 강연 게시글 객체를 생성하여 리스트에 추가
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

                postList.add(new LecturePost(lectureId, userId, userName, title, content, location, createdAt, completedAt, fee, views));
            }
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 불러오기 실패", e); // 오류 로그 출력
        }
        return postList; // 강연 게시글 리스트 반환
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 특정 ID의 강연 게시글을 가져오는 메서드
    public LecturePost getLecturePostById(int lectureId) { // 메서드명 수정
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

                    return new LecturePost(lectureId, userId, userName, title, content, location, createdAt, completedAt, fee, views);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "ID로 강연 게시글 불러오기 실패", e); // 오류 로그 출력
        }
        return null; // 강연 게시글이 없을 경우 null 반환
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 강연 게시글의 조회수를 증가시키는 메서드
    public void incrementLecturePostViews(int lectureId) { // 메서드명 수정
        String sql = "UPDATE Lecture SET Views = Views + 1 WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            pstmt.executeUpdate(); // 조회수 증가 쿼리 실행
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 조회수 업데이트 실패", e); // 오류 로그 출력
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 강연 게시글을 삭제하는 메서드
    public boolean deleteLecturePost(int lectureId) { // 메서드명 수정
        String sql = "DELETE FROM Lecture WHERE LectureID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lectureId);
            int rowsAffected = pstmt.executeUpdate(); // 강연 게시글 삭제 쿼리 실행
            return rowsAffected > 0; // 삭제 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 삭제 실패", e); // 오류 로그 출력
            return false; // 삭제 실패 시 false 반환
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 강연 게시글을 업데이트하는 메서드
    public boolean updateLecturePost(int lectureId, String title, String content, String location, double fee, int userId) { // 메서드명 수정
        String sql = "UPDATE Lecture SET Title = ?, Content = ?, Location = ?, Fee = ? WHERE LectureID = ? AND UserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, location);
            pstmt.setDouble(4, fee);
            pstmt.setInt(5, lectureId);
            pstmt.setInt(6, userId);

            int rowsAffected = pstmt.executeUpdate(); // 강연 게시글 업데이트 쿼리 실행
            return rowsAffected > 0; // 업데이트 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "강연 게시글 업데이트 실패", e); // 오류 로그 출력
            return false; // 업데이트 실패 시 false 반환
        }
    }
}
