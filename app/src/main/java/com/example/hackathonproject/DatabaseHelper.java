package com.example.hackathonproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import android.util.Log;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String URL = "jdbc:mysql://projectdb.cno4e4q0ev10.ap-northeast-2.rds.amazonaws.com:3306/project";  // RDS 엔드포인트
    private static final String USER = "admin";
    private static final String PASSWORD = "inhatc2024";

    public DatabaseHelper() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Log.d(TAG, "MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL JDBC Driver not found", e);
        }
    }

    public Connection connect() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Log.d(TAG, "Database connected!");
        } catch (SQLException e) {
            Log.e(TAG, "Failed to connect to database", e);
            throw new SQLException("Failed to connect to database", e);
        }
        return conn;
    }

    public void registerUser(String username, String password) {
        String sql = "INSERT INTO test (id, password) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            Log.d(TAG, "User registered: " + username);
        } catch (SQLException e) {
            Log.e(TAG, "Failed to register user", e);
        }
    }
}
