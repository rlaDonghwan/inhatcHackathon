package com.example.hackathonproject.db;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnection"; // 로그 태그 정의
    private static final String URL = "jdbc:mysql://projectdb.cno4e4q0ev10.ap-northeast-2.rds.amazonaws.com:3306/project?useSSL=false"; // 데이터베이스 URL
    private static final String USER = "admin"; // 데이터베이스 사용자명
    private static final String PASSWORD = "inhatc2024"; // 데이터베이스 비밀번호

    // 데이터베이스 연결 메서드
    public Connection connect() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD); // 데이터베이스에 연결 시도
            Log.d(TAG, "Database connected!"); // 연결 성공 시 로그 출력
        } catch (SQLException e) {
            Log.e(TAG, "Failed to connect to database", e); // 연결 실패 시 로그 출력
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e); // 예외 발생 시 상세 메시지와 함께 SQLException 던짐
        }
        return conn; // 연결 객체 반환
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}


