package com.example.hackathonproject.db;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

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
import java.time.format.DateTimeParseException;
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

                    // Convert the TIMESTAMP from the database to LocalDateTime with KST timezone
                    Timestamp timestamp = resultSet.getTimestamp("SentTime");
                    if (timestamp != null) {
                        LocalDateTime sentTime = timestamp.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
                        message.setSentTime(sentTime);
                    }

                    // Add the loggedInUserId to the log
                    Log.d("ChatAdapter", "Message: " + message.getMessageText() + ", Sender: " + message.getSenderUserId() + ", LoggedInUser: " + loggedInUserId);

                    messages.add(message);
                }
            }
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

                // 메시지 추가 후 로그 출력
                Log.d("ChatAdapter", "Message: " + messageText + ", Sender: " + senderUserId + ", SentTime: " + formattedDateTime);

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
