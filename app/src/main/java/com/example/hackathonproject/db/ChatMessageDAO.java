package com.example.hackathonproject.db;

import android.util.Log;
import com.example.hackathonproject.Chat.ChatMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDAO {

    private static final String TAG = "ChatMessageDAO";
    private Connection connection;

    // 생성자: 데이터베이스 연결 객체를 초기화
    public ChatMessageDAO(Connection connection) {
        this.connection = connection;
        if (connection == null) {
            Log.e(TAG, "Database connection failed. Connection is null.");
            throw new IllegalStateException("Database connection is not established.");
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 특정 채팅방의 모든 메시지를 가져오는 메서드
    public List<ChatMessage> getMessagesByChatId(int chatId, int loggedInUserId) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        String query = "SELECT MessageID, ChatID, SenderUserID, MessageText, SentTime FROM ChatMessage WHERE ChatID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, chatId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ChatMessage message = new ChatMessage();
                    message.setMessageId(resultSet.getInt("MessageID"));
                    message.setChatId(resultSet.getInt("ChatID"));
                    message.setSenderUserId(resultSet.getInt("SenderUserID"));
                    message.setMessageText(resultSet.getString("MessageText"));

                    // SentTime을 LocalDateTime으로 변환하여 설정
                    Timestamp timestamp = resultSet.getTimestamp("SentTime");
                    if (timestamp != null) {
                        LocalDateTime sentTime = timestamp.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
                        message.setSentTime(sentTime);
                    }

                    Log.d("ChatAdapter", "Message: " + message.getMessageText() + ", Sender: " + message.getSenderUserId() + ", LoggedInUser: " + loggedInUserId);

                    messages.add(message);
                }
            }
        }
        return messages;
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 새로운 메시지를 데이터베이스에 추가하는 메서드
    public boolean addMessage(int chatId, int senderUserId, String messageText, ZonedDateTime kstTime) {
        String query = "INSERT INTO ChatMessage (ChatID, SenderUserID, MessageText, SentTime) VALUES (?, ?, ?, ?)";
        String updateReadStatusQuery;

        try (Connection conn = this.connection != null && !this.connection.isClosed() ? this.connection : DatabaseConnection.getInstance().connect();
             PreparedStatement statement = conn.prepareStatement(query);
             PreparedStatement getChatDetailsStmt = conn.prepareStatement("SELECT AuthorID FROM Chat WHERE ChatID = ?");
             PreparedStatement updateReadStatusStatement = conn.prepareStatement("")) {

            statement.setInt(1, chatId);
            statement.setInt(2, senderUserId);
            statement.setString(3, messageText);
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            statement.setString(4, formattedDateTime);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                Log.i(TAG, "Message successfully added.");

                // 채팅의 AuthorID를 가져와서 메시지 읽음 상태를 업데이트
                getChatDetailsStmt.setInt(1, chatId);
                ResultSet rs = getChatDetailsStmt.executeQuery();
                int authorId = -1;
                if (rs.next()) {
                    authorId = rs.getInt("AuthorID");
                }

                // 현재 메시지 전송자가 Author인지 OtherUser인지에 따라 읽음 상태를 업데이트
                if (authorId == senderUserId) {
                    updateReadStatusQuery = "UPDATE Chat SET IsOtherUserMessageRead = FALSE WHERE ChatID = ?";
                } else {
                    updateReadStatusQuery = "UPDATE Chat SET IsAuthorMessageRead = FALSE WHERE ChatID = ?";
                }
                // 읽음 상태를 업데이트
                try (PreparedStatement updateReadStatus = conn.prepareStatement(updateReadStatusQuery)) {
                    updateReadStatus.setInt(1, chatId);
                    updateReadStatus.executeUpdate();
                }

                // 마지막 메시지와 그 시간을 업데이트
                String updateChatQuery = "UPDATE Chat SET LastMessage = ?, LastMessageTime = ? WHERE ChatID = ?";
                try (PreparedStatement updateChatStmt = conn.prepareStatement(updateChatQuery)) {
                    updateChatStmt.setString(1, messageText);
                    updateChatStmt.setString(2, formattedDateTime);
                    updateChatStmt.setInt(3, chatId);
                    updateChatStmt.executeUpdate();
                }

                // 자동 커밋이 꺼져 있다면 명시적으로 커밋
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                return true;
            } else {
                Log.e(TAG, "Failed to add message.");
                return false;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error while adding message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}