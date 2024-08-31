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

    // 생성자: 데이터베이스 연결 객체를 초기화
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

                    boolean isAuthorMessageRead = resultSet.getBoolean("IsAuthorMessageRead");
                    boolean isOtherUserMessageRead = resultSet.getBoolean("IsOtherUserMessageRead");

                    // 상대방의 이름을 결정 (로그인한 사용자가 글쓴이인지 확인)
                    String otherUserName = userId == authorId ? resultSet.getString("UserName2") : resultSet.getString("UserName1");

                    // 메시지 시간을 KST로 변환
                    LocalDateTime lastMessageTimeKST = null;
                    if (lastMessageTime != null) {
                        lastMessageTime = lastMessageTime.split("\\.")[0]; // ".0" 부분을 제거
                        lastMessageTimeKST = LocalDateTime.parse(lastMessageTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                .atZone(ZoneId.of("Asia/Seoul"))
                                .toLocalDateTime();
                    }

                    // Chat 객체 생성 시 읽음 상태를 포함하도록 수정
                    Chat chat = new Chat(chatId, authorId, otherUserId, lastMessage,
                            lastMessageTimeKST != null ? lastMessageTimeKST.toString() : null,
                            educationId, lectureId, isAuthorMessageRead, isOtherUserMessageRead);

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
                Log.d(TAG, "Received educationId: " + educationId + ", lectureId: " + lectureId);

                connection.setAutoCommit(false);

                int authorId = -1;

                // EducationID 또는 LectureID를 기반으로 AuthorID 가져오기
                if (educationId != null) {
                    authorId = getUserIdByEducationId(educationId);
                    if (authorId == -1) {
                        connection.rollback(); // 오류 발생 시 트랜잭션 롤백
                        callback.onError(new SQLException("유효하지 않은 EducationID: " + educationId));
                        return;
                    }
                } else if (lectureId != null) {
                    authorId = getUserIdByLectureId(lectureId);
                    if (authorId == -1) {
                        connection.rollback(); // 오류 발생 시 트랜잭션 롤백
                        callback.onError(new SQLException("유효하지 않은 LectureID: " + lectureId));
                        return;
                    }
                }

                int finalOtherUserId = loggedInUserId;

                String query = "SELECT ChatID FROM Chat WHERE " +
                        "AuthorID = ? AND OtherUserID = ? " +
                        "AND (EducationID = ? OR EducationID IS NULL) AND (LectureID = ? OR LectureID IS NULL)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, authorId);
                    statement.setInt(2, finalOtherUserId);
                    if (educationId != null) {
                        statement.setInt(3, educationId);
                    } else {
                        statement.setNull(3, java.sql.Types.INTEGER);
                    }
                    if (lectureId != null) {
                        statement.setInt(4, lectureId);
                    } else {
                        statement.setNull(4, java.sql.Types.INTEGER);
                    }

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int chatId = resultSet.getInt("ChatID");
                            connection.commit(); // 명시적으로 트랜잭션 커밋
                            Log.d(TAG, "채팅방이 발견됨, 트랜잭션 커밋");
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
                                            connection.commit(); // 명시적으로 트랜잭션 커밋
                                            Log.d(TAG, "채팅방이 생성됨, 트랜잭션 커밋");
                                            callback.onSuccess(chatId);
                                        } else {
                                            connection.rollback(); // 명시적으로 트랜잭션 롤백
                                            Log.e(TAG, "생성된 ChatID를 가져오지 못함, 트랜잭션 롤백");
                                            callback.onError(new SQLException("생성된 ChatID를 가져오는 데 실패했습니다."));
                                        }
                                    }
                                } else {
                                    connection.rollback(); // 명시적으로 트랜잭션 롤백
                                    Log.e(TAG, "새 채팅방 삽입 실패, 트랜잭션 롤백");
                                    callback.onError(new SQLException("데이터베이스에 새 채팅방을 삽입하는 데 실패했습니다."));
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                try {
                    connection.rollback(); // 예외 발생 시 트랜잭션 롤백 시도
                    Log.e(TAG, "SQLException 발생, 트랜잭션 롤백");
                } catch (SQLException rollbackEx) {
                    Log.e(TAG, "롤백 중 오류 발생: " + rollbackEx.getMessage());
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

    // EducationID로 AuthorID를 가져오는 메서드
    private int getUserIdByEducationId(int educationId) {
        String query = "SELECT UserID FROM Education WHERE EducationID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, educationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("UserID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // LectureID로 AuthorID를 가져오는 메서드
    private int getUserIdByLectureId(int lectureId) {
        String query = "SELECT UserID FROM Lecture WHERE LectureID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, lectureId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("UserID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // EducationID의 유효성을 검사하는 메서드
    private boolean isEducationIdValid(Integer educationId) {
        if (educationId == null) {
            return false; // null 값은 유효하지 않다고 가정
        }
        String query = "SELECT COUNT(*) FROM Education WHERE EducationID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, educationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // EducationID가 존재하는 경우 true 반환
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // LectureID의 유효성을 검사하는 메서드
    private boolean isLectureIdValid(Integer lectureId) {
        if (lectureId == null) {
            return true; // null 값은 유효하다고 가정
        }
        String query = "SELECT COUNT(*) FROM Lecture WHERE LectureID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, lectureId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // LectureID가 존재하는 경우 true 반환
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    public Chat getChatById(int chatId) throws SQLException {
        String query = "SELECT * FROM Chat WHERE ChatID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, chatId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Chat 객체를 생성하고 필요한 필드를 설정합니다.
                    int authorId = resultSet.getInt("AuthorID");
                    int otherUserId = resultSet.getInt("OtherUserID");
                    String lastMessage = resultSet.getString("LastMessage");
                    String lastMessageTime = resultSet.getString("LastMessageTime");
                    boolean isAuthorMessageRead = resultSet.getBoolean("IsAuthorMessageRead");
                    boolean isOtherUserMessageRead = resultSet.getBoolean("IsOtherUserMessageRead");

                    // Chat 객체 생성 후 반환
                    Chat chat = new Chat(chatId, authorId, otherUserId, lastMessage, lastMessageTime, null, null, isAuthorMessageRead, isOtherUserMessageRead);
                    return chat;
                } else {
                    Log.e(TAG, "Chat not found for chatId: " + chatId);
                    return null;
                }
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------


    // ChatRoomCallback 인터페이스 정의
    public interface ChatRoomCallback {
        void onSuccess(int chatId);
        void onError(Exception e);
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
