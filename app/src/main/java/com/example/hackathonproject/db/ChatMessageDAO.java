package com.example.hackathonproject.db;

import android.util.Log;

import com.example.hackathonproject.Chat.ChatMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            return messages; // 빈 리스트를 반환하거나, 예외를 던질 수 있습니다.
        }

        try {
            String query = "SELECT * FROM ChatMessage WHERE ChatID = ? ORDER BY SentTime ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, chatId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ChatMessage message = new ChatMessage();
                message.setMessageId(resultSet.getInt("MessageID"));
                message.setChatId(resultSet.getInt("ChatID"));
                message.setSenderUserId(resultSet.getInt("SenderUserID"));
                message.setMessageText(resultSet.getString("MessageText"));
                message.setSentTime(resultSet.getTimestamp("SentTime"));
                messages.add(message);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error while fetching messages: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public void addMessage(int chatId, int senderUserId, String messageText) {
        if (connection == null) {
            Log.e(TAG, "Connection is null, cannot add message.");
            return; // 연결이 없다면 아무 것도 하지 않습니다.
        }

        try {
            String query = "INSERT INTO ChatMessage (ChatID, SenderUserID, MessageText, SentTime) VALUES (?, ?, ?, NOW())";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, chatId);
            statement.setInt(2, senderUserId);
            statement.setString(3, messageText);
            statement.executeUpdate();
        } catch (SQLException e) {
            Log.e(TAG, "Error while adding message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
