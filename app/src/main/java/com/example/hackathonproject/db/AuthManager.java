package com.example.hackathonproject.db;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AuthManager {
    private final UserDAO userDAO; // 사용자 데이터베이스 접근 객체
    private static final String TAG = "AuthManager";

    public AuthManager() {
        this.userDAO = new UserDAO(); // UserDAO 초기화
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자를 등록하는 메서드
    public boolean registerUser(String name, String password, String phoneNum, String birthDate, boolean isOrganization, String companyName, String schoolName) throws SQLException {
        if (userDAO.isUserExist(phoneNum)) {
            throw new SQLException("User already exists with phone number: " + phoneNum);
        }

        int age = calculateAge(birthDate);
        String role = isOrganization ? "기관" : determineRole(age);

        userDAO.registerUser(name, password, phoneNum, age, role, companyName, schoolName);
        return true;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자를 로그인하고 사용자 ID 반환
    public int loginUserAndGetId(String phoneNum, String password) throws SQLException {
        return userDAO.getUserIdIfCredentialsMatch(phoneNum, password); // 자격 증명이 일치하면 사용자 ID 반환
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자 ID로 사용자 이름 조회
    public String getUserNameById(int userId) throws SQLException {
        return userDAO.getUserNameById(userId); // ID로 이름 조회
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자가 존재하는지 확인
    public boolean isUserExist(String phoneNum) throws SQLException {
        return userDAO.isUserExist(phoneNum); // 전화번호로 사용자 존재 여부 확인
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 비밀번호 변경
    public boolean changePassword(String phoneNum, String newPassword) throws SQLException {
        return userDAO.changePassword(phoneNum, newPassword); // 전화번호로 비밀번호 변경
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int calculateAge(String birthDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // 날짜 형식 지정
        try {
            LocalDate birthDateParsed = LocalDate.parse(birthDate, formatter); // 문자열을 날짜로 변환
            LocalDate currentDate = LocalDate.now(); // 현재 날짜 가져오기
            return Period.between(birthDateParsed, currentDate).getYears(); // 생년월일로 나이 계산
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + birthDate); // 날짜 형식이 잘못되었을 경우 예외 발생
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    private String determineRole(int age) {
        return age < 65 ? "청년" : "노인"; // 65세 미만은 청년, 65세 이상은 노인으로 역할 설정
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 이름 변경 메서드
    public boolean changeUserName(int userId, String newName) throws SQLException {
        return userDAO.updateUserName(userId, newName);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 계정 삭제 메서드
    public boolean deleteUserAccount(int userId) throws SQLException {
        return userDAO.deleteUser(userId);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 프로필 사진 이미지 업데이트(경로)
    public boolean updateProfileImage(int userId, byte[] imageBytes) throws SQLException {
        return userDAO.updateProfileImagePath(userId, imageBytes);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 프로필 사진 이미지 가져오기(경로)
    public byte[] getProfileImage(int userId) throws SQLException {
        return userDAO.getProfileImagePath(userId);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자 ID로 기관 여부를 확인하는 메서드 추가
    public boolean isUserOrganization(int userId) throws SQLException {
        return userDAO.isUserOrganization(userId); // UserDAO를 통해 기관 여부 확인
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 사용자 ID로 Balance 값을 가져오는 메서드
    public int getBalanceById(int userId) throws SQLException {
        return userDAO.getBalanceById(userId);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 전화번호로 사용자의 이름을 조회하는 메서드
    public String getUserNameByPhone(String phoneNum) throws SQLException {
        return userDAO.getUserNameByPhone(phoneNum);
    }

    // 사용자 ID로 회사명 또는 학교명을 조회하는 메서드
    public String getBusinessNameByUserId(int userId) throws SQLException {
        return userDAO.getBusinessNameByUserId(userId);
    }

    public String getSchoolNameByUserId(int userId) throws SQLException {
        return userDAO.getSchoolNameByUserId(userId);
    }

    public String getUserRoleByUserId(int userId) throws SQLException {
        return userDAO.getUserRoleByUserId(userId);
    }

}