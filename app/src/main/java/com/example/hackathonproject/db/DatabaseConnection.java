package com.example.hackathonproject.db;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnection"; // 로그 태그 정의
    private static final String URL = "jdbc:mysql://projectdb.cno4e4q0ev10.ap-northeast-2.rds.amazonaws.com:3306/project?useSSL=false"; // 데이터베이스 URL
    private static final String USER = "admin"; // 데이터베이스 사용자명
    private static final String PASSWORD = "inhatc2024"; // 데이터베이스 비밀번호

    // 동기적으로 데이터베이스에 연결하는 메서드
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

    // 비동기적으로 데이터베이스에 연결하는 메서드
    @SuppressLint("StaticFieldLeak")
    public void connectAsync(DatabaseCallback callback) {
        new AsyncTask<Void, Void, Connection>() {
            private SQLException exception;

            @Override
            protected Connection doInBackground(Void... voids) {
                Log.d(TAG, "Connecting to database...");
                try {
                    return DriverManager.getConnection(URL, USER, PASSWORD);
                } catch (SQLException e) {
                    exception = e;
                    Log.e(TAG, "Failed to connect to database", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Connection connection) {
                if (connection != null) {
                    Log.d(TAG, "Database connected!");
                    callback.onSuccess(connection);
                } else {
                    Log.e(TAG, "Database connection failed: " + (exception != null ? exception.getMessage() : "Unknown error"));
                    callback.onError(exception);
                }
            }
        }.execute();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // DatabaseCallback 인터페이스 정의
    public interface DatabaseCallback {
        void onSuccess(Connection connection);

        void onError(SQLException e);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
