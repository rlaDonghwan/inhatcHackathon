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
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHelper {
    //동환---------------------------------------------------------------------------------------------------------
    private static final String TAG = "DatabaseHelper";
    private static final String URL = "jdbc:mysql://projectdb.cno4e4q0ev10.ap-northeast-2.rds.amazonaws.com:3306/project";  // 올바른 RDS 엔드포인트
    private static final String USER = "admin";
    private static final String PASSWORD = "inhatc2024";

    // 데이터베이스에 연결하는 메서드
    public Connection connect() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD); // 데이터베이스 연결 시도
            Log.d(TAG, "Database connected!"); // 연결 성공 로그
        } catch (SQLException e) {
            Log.e(TAG, "Failed to connect to database", e); // 연결 실패 로그
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e); // 예외 발생
        }
        return conn;
    }

    // 사용자가 이미 존재하는지 확인하는 메서드
    public boolean isUserExist(String phoneNum) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User WHERE PhoneNumber = ?";
        Log.d(TAG, "Executing query: " + sql + " with phone number: " + phoneNum);
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum); // 하이픈이 포함된 전화번호 바인딩
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    Log.d(TAG, "User count for phone number " + phoneNum + ": " + count);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to check if user exists", e);
            throw e;
        }
        return false;
    }

    // 새로운 사용자를 데이터베이스에 등록하는 메서드
    public void registerUser(String name, String password, String phoneNum, String birthDate, boolean isOrganization) throws SQLException {
        // 사용자가 이미 존재하는지 확인
        if (isUserExist(phoneNum)) {
            throw new SQLException("User already exists with phone number: " + phoneNum); // 사용자 이미 존재 예외
        }

        int age = -1; // 나이 초기화
        String role = null; // 역할 초기화
        if (isOrganization) {
            role = "기관"; // 기관 사용자 역할 설정
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                age = calculateAge(birthDate); // 나이 계산
                role = determineRole(age); // 사용자 역할 결정
            }
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // 비밀번호 해싱

        Log.d(TAG, "Registering user: Name=" + name + ", PhoneNumber=" + phoneNum + ", Age=" + age + ", Role=" + role);

        String sql = "INSERT INTO User (Name, Password, PhoneNumber, Age, Role) VALUES (?, ?, ?, ?, ?)"; // 사용자 등록 쿼리
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); // 이름 바인딩
            pstmt.setString(2, hashedPassword); // 해싱된 비밀번호 바인딩
            pstmt.setString(3, phoneNum); // 전화번호 바인딩
            pstmt.setInt(4, age); // 나이 바인딩
            pstmt.setString(5, role); // 역할 바인딩
            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            Log.d(TAG, "User registered: " + name + ", Rows affected: " + rowsAffected); // 사용자 등록 로그
        } catch (SQLException e) {
            Log.e(TAG, "Failed to register user: " + e.getErrorCode() + " - " + e.getSQLState(), e); // 등록 실패 로그
            throw e; // 예외 발생
        }
    }

    // 생년월일을 기반으로 나이를 계산하는 메서드
    @RequiresApi(api = Build.VERSION_CODES.O)
    private int calculateAge(String birthDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // 날짜 포맷터 설정
        try {
            LocalDate birth = LocalDate.parse(birthDate, inputFormatter); // 생년월일 파싱
            LocalDate today = LocalDate.now(); // 현재 날짜
            return Period.between(birth, today).getYears(); // 나이 계산
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Failed to parse birth date: " + birthDate, e); // 파싱 실패 로그
            throw e; // 예외 발생
        }
    }

    // 나이에 따라 사용자 역할을 결정하는 메서드
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String determineRole(int age) {
        return age < 65 ? "청년" : "노인"; // 나이에 따라 역할 반환
    }

    // 사용자 인증 메서드
    public String loginUser(String phoneNum, String password) throws SQLException {
        String sql = "SELECT Name, Password FROM User WHERE PhoneNumber = ?"; // 사용자 인증 쿼리
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum); // 전화번호 바인딩
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("Password"); // 저장된 비밀번호 가져오기
                    if (BCrypt.checkpw(password, storedPassword)) { // 비밀번호 검증
                        return rs.getString("Name"); // 이름 반환
                    } else {
                        Log.d(TAG, "Invalid password for phone number: " + phoneNum); // 비밀번호 불일치 로그
                        return null;
                    }
                } else {
                    Log.d(TAG, "No user found with phone number: " + phoneNum); // 사용자 미존재 로그
                    return null;
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to login user", e); // 로그인 실패 로그
            throw e; // 예외 발생
        }
    }

    // 비밀번호 변경 메서드
    public boolean changePassword(String phoneNum, String newPassword) throws SQLException {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt()); // 새 비밀번호 해싱
        String sql = "UPDATE User SET Password = ? WHERE PhoneNumber = ?"; // 비밀번호 변경 쿼리
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword); // 해싱된 비밀번호 바인딩
            pstmt.setString(2, phoneNum); // 전화번호 바인딩
            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            Log.d(TAG, "Password updated for phone number: " + phoneNum + ", Rows affected: " + rowsAffected); // 비밀번호 변경 로그
            return rowsAffected > 0; // 변경 성공 여부 반환
        } catch (SQLException e) {
            Log.e(TAG, "Failed to change password: " + e.getErrorCode() + " - " + e.getSQLState(), e); // 변경 실패 로그
            throw e; // 예외 발생
        }
    }
    //동환---------------------------------------------------------------------------------------------------------

    //----------------------------------------------주진DB------------------------------------------------------------------------------
    public boolean insertEducation(String title, String description) {
        String sql = "INSERT INTO Education (Title, Description, Date, OrganizerID, OrganizerRole, EducatorID, Location, Status) " +
                "VALUES (?, ?, CURDATE(), 1, '기관', 1, '서울', '등록')";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert education", e);
            return false;
        }
    }

    // AsyncTask를 사용하여 교육 정보를 비동기로 삽입하는 메서드
    public void insertEducationAsync(String title, String description, DatabaseCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return insertEducation(title, description);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callback != null) {
                    callback.onQueryComplete(result);
                }
            }
        }.execute();
    }

    // AsyncTask를 사용하여 교육 정보를 비동기로 가져오는 메서드
    public void getAllEducationsAsync(DatabaseCallback callback) {
        new AsyncTask<Void, Void, List<Education>>() {
            @Override
            protected List<Education> doInBackground(Void... voids) {
                return getAllEducations();
            }

            @Override
            protected void onPostExecute(List<Education> educationList) {
                if (callback != null) {
                    callback.onQueryComplete(educationList);
                }
            }
        }.execute();
    }

    public List<Education> getAllEducations() {
        List<Education> educationList = new ArrayList<>();
        String sql = "SELECT Title, Description, Location, Status FROM Education";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String title = rs.getString("Title");
                String description = rs.getString("Description");
                String location = rs.getString("Location");
                int views = 0; // 조회수는 따로 관리하거나 계산하는 로직이 필요
                educationList.add(new Education(title, description, location, views));
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load educations", e);
        }

        return educationList;
    }

    // 콜백 인터페이스
    public interface DatabaseCallback {
        void onQueryComplete(Object result);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------
}

