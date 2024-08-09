package com.example.hackathonproject.db;

import android.util.Log;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private static final String TAG = "UserDAO"; // 로그 태그
    private DatabaseConnection dbConnection = new DatabaseConnection(); // 데이터베이스 연결 객체

    // 사용자가 존재하는지 확인하는 메서드
    public boolean isUserExist(String phoneNum) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User WHERE PhoneNumber = ?"; // 전화번호로 사용자 존재 여부 확인

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum); // 전화번호 설정
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // 사용자가 존재하면 true 반환
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to check if user exists", e); // 오류 로그 출력
            throw e; // 예외 다시 던지기
        }
        return false; // 사용자가 존재하지 않으면 false 반환
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자를 등록하는 메서드
    public void registerUser(String name, String password, String phoneNum, int age, String role) throws SQLException {
        String sql = "INSERT INTO User (Name, Password, PhoneNumber, Age, Role) VALUES (?, ?, ?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // 비밀번호 해시 처리

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); // 이름 설정
            pstmt.setString(2, hashedPassword); // 해시된 비밀번호 설정
            pstmt.setString(3, phoneNum); // 전화번호 설정
            pstmt.setInt(4, age); // 나이 설정
            pstmt.setString(5, role); // 역할 설정
            pstmt.executeUpdate(); // 사용자 등록 쿼리 실행
        } catch (SQLException e) {
            Log.e(TAG, "Failed to register user", e); // 오류 로그 출력
            throw e; // 예외 다시 던지기
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 자격 증명이 일치하는 경우 사용자 ID를 반환하는 메서드
    public int getUserIdIfCredentialsMatch(String phoneNum, String password) throws SQLException {
        String sql = "SELECT UserID, Password FROM User WHERE PhoneNumber = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum); // 전화번호 설정
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("Password");
                    Log.d(TAG, "Stored password: " + storedPassword);  // 로그 추가
                    if (BCrypt.checkpw(password, storedPassword)) { // 비밀번호 일치 여부 확인
                        int userId = rs.getInt("UserID");
                        Log.d(TAG, "User ID: " + userId);  // 로그 추가
                        return userId; // 일치하면 사용자 ID 반환
                    } else {
                        Log.d(TAG, "Invalid password for phone number: " + phoneNum);
                        return -1; // 비밀번호가 일치하지 않으면 -1 반환
                    }
                } else {
                    Log.d(TAG, "No user found with phone number: " + phoneNum);
                    return -1; // 사용자가 없으면 -1 반환
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to login user", e); // 오류 로그 출력
            throw e; // 예외 다시 던지기
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자 ID로 사용자 이름을 가져오는 메서드
    public String getUserNameById(int userId) throws SQLException {
        String sql = "SELECT Name FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId); // 사용자 ID 설정
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name"); // 사용자 이름 반환
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get user name by ID", e); // 오류 로그 출력
            throw e; // 예외 다시 던지기
        }
        return null; // 사용자 이름이 없으면 null 반환
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 비밀번호를 변경하는 메서드
    public boolean changePassword(String phoneNum, String newPassword) throws SQLException {
        String sql = "UPDATE User SET Password = ? WHERE PhoneNumber = ?";
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt()); // 새 비밀번호 해시 처리

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword); // 해시된 새 비밀번호 설정
            pstmt.setString(2, phoneNum); // 전화번호 설정
            int rowsAffected = pstmt.executeUpdate(); // 비밀번호 변경 쿼리 실행
            return rowsAffected > 0; // 변경 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to change password", e); // 오류 로그 출력
            throw e; // 예외 다시 던지기
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
