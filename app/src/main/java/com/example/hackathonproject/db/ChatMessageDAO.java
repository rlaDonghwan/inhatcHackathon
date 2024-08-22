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

    public ChatMessageDAO(Connection connection) {
        this.connection = connection;
        if (connection == null) {
            Log.e(TAG, "Database connection failed. Connection is null.");
            throw new IllegalStateException("Database connection is not established.");
        }
    }

    public List<ChatMessage> getMessagesByChatId(int chatId) {
        List<ChatMessage> messages = new ArrayList<>();
        if (connection == null) {
            Log.e(TAG, "Connection is null, cannot fetch messages.");
            return messages;
        }

        try {
            String query = "SELECT * FROM ChatMessage WHERE ChatID = ? ORDER BY SentTime ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, chatId);

            Log.d(TAG, "Executing query: " + statement.toString());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ChatMessage message = new ChatMessage();
                message.setMessageId(resultSet.getInt("MessageID"));
                message.setChatId(resultSet.getInt("ChatID"));
                message.setSenderUserId(resultSet.getInt("SenderUserID"));
                message.setMessageText(resultSet.getString("MessageText"));

                // Convert the TIMESTAMP from the database to LocalDateTime with KST timezone
                Timestamp timestamp = resultSet.getTimestamp("SentTime");
                if (timestamp != null) {
                    LocalDateTime sentTime = timestamp.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
                    message.setSentTime(sentTime);
                }

                Log.d(TAG, "Fetched message: " + message.getMessageText());

                messages.add(message);
            }

            if (messages.isEmpty()) {
                Log.w(TAG, "No messages found for ChatID: " + chatId);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error while fetching messages: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public boolean addMessage(int chatId, int senderUserId, String messageText, ZonedDateTime kstTime) {
        String query = "INSERT INTO ChatMessage (ChatID, SenderUserID, MessageText, SentTime) VALUES (?, ?, ?, ?)";

        try (Connection conn = this.connection != null && !this.connection.isClosed() ? this.connection : new DatabaseConnection().connect();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, chatId);
            statement.setInt(2, senderUserId);
            statement.setString(3, messageText);

            // ZonedDateTime을 포맷팅된 문자열로 변환하여 저장
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            statement.setString(4, formattedDateTime); // DATETIME 형식으로 저장

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                Log.i(TAG, "Message successfully added.");

                // Update the LastMessage and LastMessageTime in the Chat table
                String updateChatQuery = "UPDATE Chat SET LastMessage = ?, LastMessageTime = ? WHERE ChatID = ?";
                try (PreparedStatement updateChatStmt = conn.prepareStatement(updateChatQuery)) {
                    updateChatStmt.setString(1, messageText);
                    updateChatStmt.setString(2, formattedDateTime); // 같은 KST 시간 사용
                    updateChatStmt.setInt(3, chatId);
                    updateChatStmt.executeUpdate();

                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }
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


}
