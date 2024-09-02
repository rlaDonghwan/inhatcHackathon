package com.example.hackathonproject.db;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        // Singleton 패턴 적용: 외부에서 객체를 생성하지 못하도록 생성자를 private으로 설정
        initializeConnectionPoolAsync(); // 비동기적으로 연결 풀 초기화
    }

    // Singleton 인스턴스 반환
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // 비동기적으로 연결 풀을 초기화하는 메서드
    @SuppressLint("StaticFieldLeak")
    private void initializeConnectionPoolAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                initializeConnectionPool();
                return null;
            }
        }.execute();
    }

    // HikariCP를 사용하여 연결 풀을 초기화하는 메서드
    private void initializeConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://projectdb.cno4e4q0ev10.ap-northeast-2.rds.amazonaws.com:3306/project");
        config.setUsername("admin");
        config.setPassword("inhatc2024");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        try {
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            Log.e("DatabaseConnection", "Failed to initialize pool: " + e.getMessage());
        }
    }

    // 데이터베이스에 연결하는 메서드
    public Connection connect() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized.");
        }
        return dataSource.getConnection();
    }

    // 비동기적으로 데이터베이스에 연결하고 콜백을 통해 결과를 반환하는 메서드
    @SuppressLint("StaticFieldLeak")
    public void connectAsync(DatabaseCallback callback) {
        new AsyncTask<Void, Void, Connection>() {
            private SQLException exception;

            @Override
            protected Connection doInBackground(Void... voids) {
                try {
                    return connect();
                } catch (SQLException e) {
                    exception = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Connection connection) {
                if (connection != null) {
                    callback.onSuccess(connection);
                } else {
                    callback.onError(exception);
                }
            }
        }.execute();
    }

    // 데이터베이스 연결 결과를 처리하기 위한 콜백 인터페이스
    public interface DatabaseCallback {
        void onSuccess(Connection connection);  // 연결이 성공했을 때 호출
        void onError(SQLException e);  // 연결이 실패했을 때 호출
    }
}