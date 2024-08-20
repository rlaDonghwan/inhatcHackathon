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
            return messages;
        }

        try {
            String query = "SELECT * FROM ChatMessage WHERE ChatID = ? ORDER BY SentTime ASC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, chatId);

            Log.d(TAG, "Executing query: " + statement.toString()); // 쿼리 로그

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ChatMessage message = new ChatMessage();
                message.setMessageId(resultSet.getInt("MessageID"));
                message.setChatId(resultSet.getInt("ChatID"));
                message.setSenderUserId(resultSet.getInt("SenderUserID"));
                message.setMessageText(resultSet.getString("MessageText"));
                message.setSentTime(resultSet.getTimestamp("SentTime"));

                Log.d(TAG, "Fetched message: " + message.getMessageText()); // 메시지 로그

                messages.add(message);
            }

            if (messages.isEmpty()) {
                Log.w(TAG, "No messages found for ChatID: " + chatId); // 메시지가 없을 경우 경고 로그
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
            return;
        }

        try {
            // 메시지 추가
            String query = "INSERT INTO ChatMessage (ChatID, SenderUserID, MessageText, SentTime) VALUES (?, ?, ?, NOW())";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, chatId);
            statement.setInt(2, senderUserId);
            statement.setString(3, messageText);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                Log.i(TAG, "Message successfully added.");

                // 채팅방의 마지막 메시지와 시간을 업데이트
                String updateChatQuery = "UPDATE Chat SET LastMessage = ?, LastMessageTime = NOW() WHERE ChatID = ?";
                PreparedStatement updateChatStmt = connection.prepareStatement(updateChatQuery);
                updateChatStmt.setString(1, messageText);
                updateChatStmt.setInt(2, chatId);
                updateChatStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();  // 트랜잭션이 수동 관리되는 경우 커밋
                }
            } else {
                Log.e(TAG, "Failed to add message.");
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error while adding message: " + e.getMessage());
            e.printStackTrace();
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();  // 트랜잭션 실패 시 롤백
                }
            } catch (SQLException rollbackEx) {
                Log.e(TAG, "Error during rollback: " + rollbackEx.getMessage());
            }
        }
    }


}
