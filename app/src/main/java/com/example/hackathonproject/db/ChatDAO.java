package com.example.hackathonproject.db;

import static android.content.ContentValues.TAG;

import android.util.Log;
import com.example.hackathonproject.Chat.Chat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    private Connection connection;

    public ChatDAO(Connection connection) {
        this.connection = connection;
    }

    // 특정 사용자가 속한 모든 채팅방을 가져오는 메서드
    public List<Chat> getAllChatsForUser(int userId) {
        List<Chat> chatList = new ArrayList<>();
        String query = "SELECT c.*, u1.Name AS UserName1, u2.Name AS UserName2 " +
                "FROM Chat c " +
                "JOIN User u1 ON c.AuthorID = u1.UserID " +
                "JOIN User u2 ON c.OtherUserID = u2.UserID " +
                "WHERE c.AuthorID = ? OR c.OtherUserID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int chatId = resultSet.getInt("ChatID");
                    int authorId = resultSet.getInt("AuthorID");
                    int otherUserId = resultSet.getInt("OtherUserID");
                    Integer educationId = resultSet.getObject("EducationID") != null ? resultSet.getInt("EducationID") : null;
                    Integer lectureId = resultSet.getObject("LectureID") != null ? resultSet.getInt("LectureID") : null;
                    String lastMessage = resultSet.getString("LastMessage");
                    String lastMessageTime = resultSet.getString("LastMessageTime");

                    String otherUserName = userId == authorId ? resultSet.getString("UserName2") : resultSet.getString("UserName1");

                    // KST로 시간을 변환
                    LocalDateTime lastMessageTimeKST = null;
                    if (lastMessageTime != null) {
                        lastMessageTime = lastMessageTime.split("\\.")[0]; // ".0" 부분을 제거
                        lastMessageTimeKST = LocalDateTime.parse(lastMessageTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                .atZone(ZoneId.of("Asia/Seoul"))
                                .toLocalDateTime();
                    }

                    // Chat 객체 생성 시 lectureId를 포함하도록 수정
                    Chat chat = new Chat(chatId, authorId, otherUserId, lastMessage, lastMessageTimeKST != null ? lastMessageTimeKST.toString() : null, educationId, lectureId);

                    chat.setOtherUserName(otherUserName);
                    chatList.add(chat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chatList;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 채팅방을 가져오거나 새로 생성하는 메서드
    public void getOrCreateChatRoom(int loggedInUserId, int otherUserId, Integer educationId, Integer lectureId, ChatRoomCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "----------------Received educationId: " + educationId + ", lectureId: " + lectureId);

                // 작은 ID 값을 항상 AuthorID로 설정, 큰 ID 값을 OtherUserID로 설정
                int authorId = Math.min(loggedInUserId, otherUserId);
                int finalOtherUserId = Math.max(loggedInUserId, otherUserId);

                connection.setAutoCommit(false);

                // EducationID 또는 LectureID가 제공된 경우 유효성 검사
                if (educationId != null && !isEducationIdValid(educationId)) {
                    connection.rollback();
                    Log.e(TAG, "EducationID is invalid, rolling back transaction");
                    callback.onError(new SQLException("Invalid EducationID: " + educationId));
                    return;
                }

                if (lectureId != null && !isLectureIdValid(lectureId)) {
                    connection.rollback();
                    Log.e(TAG, "LectureID is invalid, rolling back transaction");
                    callback.onError(new SQLException("Invalid LectureID: " + lectureId));
                    return;
                }

                String query = "SELECT ChatID FROM Chat WHERE " +
                        "((AuthorID = ? AND OtherUserID = ?) OR (AuthorID = ? AND OtherUserID = ?)) " +
                        "AND (EducationID = ? OR EducationID IS NULL) AND (LectureID = ? OR LectureID IS NULL)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, authorId);
                    statement.setInt(2, finalOtherUserId);
                    statement.setInt(3, finalOtherUserId);
                    statement.setInt(4, authorId);
                    if (educationId != null) {
                        statement.setInt(5, educationId);
                    } else {
                        statement.setNull(5, java.sql.Types.INTEGER);
                    }
                    if (lectureId != null) {
                        statement.setInt(6, lectureId);
                    } else {
                        statement.setNull(6, java.sql.Types.INTEGER);
                    }

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int chatId = resultSet.getInt("ChatID");
                            connection.commit(); // 명시적으로 commit 호출
                            Log.d(TAG, "Chat room found, committing transaction");
                            callback.onSuccess(chatId);
                        } else {
                            query = "INSERT INTO Chat (AuthorID, OtherUserID, EducationID, LectureID) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement insertStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                insertStatement.setInt(1, authorId);
                                insertStatement.setInt(2, finalOtherUserId);
                                if (educationId != null) {
                                    insertStatement.setInt(3, educationId);
                                } else {
                                    insertStatement.setNull(3, java.sql.Types.INTEGER);
                                }
                                if (lectureId != null) {
                                    insertStatement.setInt(4, lectureId);
                                } else {
                                    insertStatement.setNull(4, java.sql.Types.INTEGER);
                                }
                                int rowsAffected = insertStatement.executeUpdate();

                                if (rowsAffected > 0) {
                                    try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                                        if (generatedKeys.next()) {
                                            int chatId = generatedKeys.getInt(1);
                                            connection.commit(); // 명시적으로 commit 호출
                                            Log.d(TAG, "Chat room created, committing transaction");
                                            callback.onSuccess(chatId);
                                        } else {
                                            connection.rollback(); // 명시적으로 rollback 호출
                                            Log.e(TAG, "Failed to retrieve generated ChatID, rolling back transaction");
                                            callback.onError(new SQLException("Failed to retrieve the generated ChatID."));
                                        }
                                    }
                                } else {
                                    connection.rollback(); // 명시적으로 rollback 호출
                                    Log.e(TAG, "Failed to insert new chat room, rolling back transaction");
                                    callback.onError(new SQLException("Failed to insert the new chat room into the database."));
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                try {
                    connection.rollback(); // 예외가 발생하면 rollback 시도
                    Log.e(TAG, "SQLException occurred, rolling back transaction");
                } catch (SQLException rollbackEx) {
                    Log.e(TAG, "Error during rollback: " + rollbackEx.getMessage());
                }
                e.printStackTrace();
                callback.onError(e);
            } finally {
                try {
                    connection.setAutoCommit(true); // 작업이 끝난 후 autoCommit을 다시 true로 설정
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    private boolean isEducationIdValid(Integer educationId) {
        if (educationId == null) {
            return false; // null 값은 유효하지 않다고 가정
        }
        String query = "SELECT COUNT(*) FROM Education WHERE EducationID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, educationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // `EducationID`가 존재하는 경우 `true` 반환
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isLectureIdValid(Integer lectureId) {
        if (lectureId == null) {
            return true; // null 값은 유효하다고 가정할 수 있습니다.
        }
        String query = "SELECT COUNT(*) FROM Lecture WHERE LectureID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, lectureId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // ChatRoomCallback 인터페이스 정의
    public interface ChatRoomCallback {
        void onSuccess(int chatId);
        void onError(Exception e);
    }
}
