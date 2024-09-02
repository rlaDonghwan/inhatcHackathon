package com.example.hackathonproject.db;

import android.util.Log;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private static final String TAG = "UserDAO";
    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance(); // Use Singleton instance

    // 사용자가 존재하는지 확인하는 메서드
    public boolean isUserExist(String phoneNum) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User WHERE PhoneNumber = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to check if user exists", e);
            throw e;
        }
        return false;
    }

    // 사용자를 등록하는 메서드
    public void registerUser(String name, String password, String phoneNum, int age, String role, String companyName, String schoolName) throws SQLException {
        String sql = "INSERT INTO User (Name, Password, PhoneNumber, Age, Role, CompanyName, SchoolName) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, phoneNum);
            pstmt.setInt(4, age);
            pstmt.setString(5, role);
            pstmt.setString(6, companyName);
            pstmt.setString(7, schoolName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Log.e(TAG, "Failed to register user", e);
            throw e;
        }
    }

    // 자격 증명이 일치하는 경우 사용자 ID를 반환하는 메서드
    public int getUserIdIfCredentialsMatch(String phoneNum, String password) throws SQLException {
        String sql = "SELECT UserID, Password FROM User WHERE PhoneNumber = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("Password");
                    if (BCrypt.checkpw(password, storedPassword)) {
                        return rs.getInt("UserID");
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to login user", e);
            throw e;
        }
        return -1;
    }

    // 사용자 ID로 사용자 이름을 가져오는 메서드
    public String getUserNameById(int userId) throws SQLException {
        String sql = "SELECT Name FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get user name by ID", e);
            throw e;
        }
        return null;
    }

    // 비밀번호를 변경하는 메서드
    public boolean changePassword(String phoneNum, String newPassword) throws SQLException {
        String sql = "UPDATE User SET Password = ? WHERE PhoneNumber = ?";
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, phoneNum);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to change password", e);
            throw e;
        }
    }

    // 사용자 이름을 업데이트하는 메서드
    public boolean updateUserName(int userId, String newName) throws SQLException {
        String sql = "UPDATE User SET Name = ? WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update user name", e);
            throw e;
        }
    }

    // 계정 삭제 메서드
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete user", e);
            throw e;
        }
    }

    // 사용자 ID로 자원봉사 시간을 가져오는 메서드
    public int getVolunteerHoursById(int userId) throws SQLException {
        String sql = "SELECT VolunteerHours FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("VolunteerHours");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get volunteer hours by ID", e);
            throw e;
        }
        return 0;
    }

    // 프로필 이미지 경로 업데이트 메서드
    public boolean updateProfileImagePath(int userId, byte[] imageBytes) throws SQLException {
        String sql = "UPDATE User SET ProfileImagePath = ? WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBytes(1, imageBytes);
            pstmt.setInt(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update profile image path", e);
            throw e;
        }
    }

    // 사용자 ID로 프로필 이미지 경로 가져오기 메서드
    public byte[] getProfileImagePath(int userId) throws SQLException {
        String sql = "SELECT ProfileImagePath FROM User WHERE UserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("ProfileImagePath");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to load profile image", e);
            throw e;
        }
        return null;
    }

    // 사용자 ID로 기관 여부를 확인하는 메서드
    public boolean isUserOrganization(int userId) throws SQLException {
        String sql = "SELECT Role FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "기관".equals(rs.getString("Role"));
                }
            }
        }
        return false;
    }

    // 사용자 ID로 Balance 값을 가져오는 메서드
    public int getBalanceById(int userId) throws SQLException {
        String sql = "SELECT Balance FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Balance");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get balance by ID", e);
            throw e;
        }
        return 0;
    }

    // 전화번호로 사용자의 이름을 조회하는 메서드
    public String getUserNameByPhone(String phoneNum) throws SQLException {
        String sql = "SELECT Name FROM User WHERE PhoneNumber = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Name");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get user name by phone number", e);
            throw e;
        }
        return null;
    }

    // 사용자 ID로 회사 또는 학교 이름을 가져오는 메서드
    public String getBusinessNameByUserId(int userId) throws SQLException {
        String sql = "SELECT CompanyName, SchoolName, Role FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if ("기관".equals(rs.getString("Role"))) {
                        return rs.getString("CompanyName");
                    } else if ("학교".equals(rs.getString("Role"))) {
                        return rs.getString("SchoolName");
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get business or school name by user ID", e);
            throw e;
        }
        return null;
    }

    // 사용자의 모든 채팅 기록을 삭제하는 메서드
    public boolean deleteUserChats(int userId) throws SQLException {
        String deleteMessagesSQL = "DELETE FROM ChatMessage WHERE ChatID IN (SELECT ChatID FROM Chat WHERE AuthorID = ? OR OtherUserID = ?)";
        String deleteChatsSQL = "DELETE FROM Chat WHERE AuthorID = ? OR OtherUserID = ?";

        try (Connection conn = dbConnection.connect();
             PreparedStatement deleteMessagesStmt = conn.prepareStatement(deleteMessagesSQL);
             PreparedStatement deleteChatsStmt = conn.prepareStatement(deleteChatsSQL)) {

            deleteMessagesStmt.setInt(1, userId);
            deleteMessagesStmt.setInt(2, userId);
            int messagesDeleted = deleteMessagesStmt.executeUpdate();
            Log.d(TAG, "Chat messages deleted: " + messagesDeleted);

            deleteChatsStmt.setInt(1, userId);
            deleteChatsStmt.setInt(2, userId);
            int chatsDeleted = deleteChatsStmt.executeUpdate();
            Log.d(TAG, "Chats deleted: " + chatsDeleted);

            return chatsDeleted > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete user chats", e);
            throw e;
        }
    }

    // 사용자의 모든 채팅방을 삭제하는 메서드
    public boolean deleteUserChatRooms(int userId) throws SQLException {
        String sql = "DELETE FROM Chat WHERE AuthorID = ? OR OtherUserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            int rowsDeleted = pstmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete user chat rooms", e);
            throw e;
        }
    }

    public String getSchoolNameByUserId(int userId) throws SQLException {
        String sql = "SELECT SchoolName FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("SchoolName");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get school name by user ID", e);
            throw e;
        }
        return null;
    }

    public String getUserRoleByUserId(int userId) throws SQLException {
        String sql = "SELECT Role FROM User WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Role");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Failed to get user role by ID", e);
            throw e;
        }
        return null;
    }

    public boolean deleteUserEducationPosts(int userId) throws SQLException {
        String sql = "DELETE FROM Education WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsDeleted = pstmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete user education posts", e);
            throw e;
        }
    }

    public boolean deleteUserLecturePosts(int userId) throws SQLException {
        String sql = "DELETE FROM Lecture WHERE UserID = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsDeleted = pstmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete user lecture posts", e);
            throw e;
        }
    }
}