package com.example.hackathonproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String URL = "jdbc:mysql://projectdb.cno4e4q0ev10.ap-northeast-2.rds.amazonaws.com:3306/project";  // 올바른 RDS 엔드포인트
    private static final String USER = "admin";
    private static final String PASSWORD = "inhatc2024";

    // 데이터베이스에 연결하는 메서드
    public Connection connect() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Log.d(TAG, "Database connected!");
        } catch (SQLException e) {
            Log.e(TAG, "Failed to connect to database", e);
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
        return conn;
    }

    // 새로운 사용자를 데이터베이스에 등록하는 메서드
    public void registerUser(String name, String password, String phoneNum, String birthDate) throws SQLException {
        int age = -1;
        String role = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            age = calculateAge(birthDate); // 나이 계산
            role = determineRole(age); // 사용자 역할 결정
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // 비밀번호 해싱

        Log.d(TAG, "Registering user: Name=" + name + ", PhoneNumber=" + phoneNum + ", Age=" + age + ", Role=" + role);

        String sql = "INSERT INTO User (Name, Password, PhoneNumber, Age, Role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, phoneNum);
            pstmt.setInt(4, age);
            pstmt.setString(5, role);
            int rowsAffected = pstmt.executeUpdate();
            Log.d(TAG, "User registered: " + name + ", Rows affected: " + rowsAffected);
        } catch (SQLException e) {
            Log.e(TAG, "Failed to register user: " + e.getErrorCode() + " - " + e.getSQLState(), e);
            throw e;
        }
    }

    // 생년월일을 기반으로 나이를 계산하는 메서드
    @RequiresApi(api = Build.VERSION_CODES.O)
    private int calculateAge(String birthDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            LocalDate birth = LocalDate.parse(birthDate, inputFormatter);
            LocalDate today = LocalDate.now();
            return Period.between(birth, today).getYears();
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Failed to parse birth date: " + birthDate, e);
            throw e;
        }
    }

    // 나이에 따라 사용자 역할을 결정하는 메서드
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String determineRole(int age) {
        return age < 65 ? "청년" : "노인";
    }

    // 사용자 인증 메서드
    public String loginUser(String phoneNum, String password) throws SQLException {
        String sql = "SELECT Name, Password FROM User WHERE PhoneNumber = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("Password");
                    if (BCrypt.checkpw(password, storedPassword)) {
                        return rs.getString("Name");
                    } else {
                        Log.d(TAG, "Invalid password for phone number: " + phoneNum);
                        return null;
                    }
                } else {
                    Log.d(TAG, "No user found with phone number: " + phoneNum);
                    return null;
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to login user", e);
            throw e;
        }
    }

}
