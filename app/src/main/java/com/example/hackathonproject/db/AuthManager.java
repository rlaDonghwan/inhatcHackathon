package com.example.hackathonproject.db;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AuthManager {
    private final UserDAO userDAO;

    public AuthManager() {
        this.userDAO = new UserDAO();
    }

    // 사용자를 등록하는 메서드
    public boolean registerUser(String name, String password, String phoneNum, String birthDate, boolean isOrganization, String companyName, String schoolName) throws SQLException {
        if (userDAO.isUserExist(phoneNum)) {
            throw new SQLException("User already exists with phone number: " + phoneNum);
        }

        int age = calculateAge(birthDate);
        String role;

        if (isOrganization) {
            role = "기관";
        } else if (schoolName != null && !schoolName.isEmpty()) {
            role = "학교";
        } else {
            role = determineRole(age);
        }

        userDAO.registerUser(name, password, phoneNum, age, role, companyName, schoolName);
        return true;
    }

    // 사용자를 로그인하고 사용자 ID 반환
    public int loginUserAndGetId(String phoneNum, String password) throws SQLException {
        return userDAO.getUserIdIfCredentialsMatch(phoneNum, password);
    }

    // 사용자 ID로 사용자 이름 조회
    public String getUserNameById(int userId) throws SQLException {
        return userDAO.getUserNameById(userId);
    }

    // 사용자가 존재하는지 확인
    public boolean isUserExist(String phoneNum) throws SQLException {
        return userDAO.isUserExist(phoneNum);
    }

    // 비밀번호 변경
    public boolean changePassword(String phoneNum, String newPassword) throws SQLException {
        return userDAO.changePassword(phoneNum, newPassword);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int calculateAge(String birthDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            LocalDate birthDateParsed = LocalDate.parse(birthDate, formatter);
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDateParsed, currentDate).getYears();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + birthDate);
        }
    }

    private String determineRole(int age) {
        return age < 65 ? "청년" : "노인";
    }

    // 이름 변경 메서드
    public boolean changeUserName(int userId, String newName) throws SQLException {
        return userDAO.updateUserName(userId, newName);
    }

    // 계정 삭제 메서드
    public boolean deleteUserAccount(int userId) throws SQLException {
        return userDAO.deleteUser(userId);
    }

    // 프로필 사진 이미지 업데이트(경로)
    public boolean updateProfileImage(int userId, String imagePath) throws SQLException {
        return userDAO.updateProfileImagePath(userId, imagePath);
    }

    // 프로필 사진 이미지 가져오기(경로)
    public String getProfileImagePath(int userId) throws SQLException {
        return userDAO.getProfileImagePath(userId);
    }

    // 사용자 ID로 기관 여부를 확인하는 메서드 추가
    public boolean isUserOrganization(int userId) throws SQLException {
        return userDAO.isUserOrganization(userId);
    }

    // 사용자 ID로 Balance 값을 가져오는 메서드
    public int getBalanceById(int userId) throws SQLException {
        return userDAO.getBalanceById(userId);
    }

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

    // 사용자의 교육 게시글을 삭제하는 메서드
    public boolean deleteUserEducationPosts(int userId) throws SQLException {
        return userDAO.deleteUserEducationPosts(userId);
    }

    // 사용자의 모든 채팅 기록을 삭제하는 메서드
    public boolean deleteUserChats(int userId) throws SQLException {
        return userDAO.deleteUserChats(userId);
    }

    // 사용자의 모든 채팅방을 삭제하는 메서드
    public boolean deleteUserChatRooms(int userId) throws SQLException {
        return userDAO.deleteUserChatRooms(userId);
    }

    public boolean deleteUserLecturePosts(int userId) throws SQLException {
        return userDAO.deleteUserLecturePosts(userId);
    }

}