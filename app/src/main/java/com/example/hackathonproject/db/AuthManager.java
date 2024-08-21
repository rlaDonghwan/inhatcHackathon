package com.example.hackathonproject.db;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AuthManager {
    private UserDAO userDAO; // 사용자 데이터베이스 접근 객체

    public AuthManager() {
        this.userDAO = new UserDAO(); // UserDAO 초기화
    }

    // 사용자를 등록하는 메서드
    public boolean registerUser(String name, String password, String phoneNum, String birthDate, boolean isOrganization) throws SQLException {
        if (userDAO.isUserExist(phoneNum)) { // 이미 등록된 사용자인지 확인
            throw new SQLException("User already exists with phone number: " + phoneNum); // 이미 존재하면 예외 발생
        }

        int age = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // SDK 버전이 O 이상인지 확인
            age = calculateAge(birthDate); // 생년월일을 통해 나이 계산
        }
        String role = isOrganization ? "기관" : determineRole(age); // 기관인지 개인인지에 따라 역할 결정

        userDAO.registerUser(name, password, phoneNum, age, role); // 사용자 등록
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

    // 나이에 따라 역할 결정
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

}
