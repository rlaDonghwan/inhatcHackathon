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

    // 생성자: 데이터베이스 연결을 초기화하고 연결이 null이면 예외를 발생시킵니다.
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

        // SQL 쿼리를 준비하고 매개변수를 설정합니다.
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, chatId);
            try (ResultSet resultSet = statement.executeQuery()) {

                // 결과 집합에서 각 메시지를 처리하여 리스트에 추가합니다.
                while (resultSet.next()) {
                    ChatMessage message = new ChatMessage();
                    message.setMessageId(resultSet.getInt("MessageID"));
                    message.setChatId(resultSet.getInt("ChatID"));
                    message.setSenderUserId(resultSet.getInt("SenderUserID"));
                    message.setMessageText(resultSet.getString("MessageText"));

                    // TIMESTAMP 형식을 데이터베이스에서 가져와 KST(LocalDateTime)로 변환합니다.
                    Timestamp timestamp = resultSet.getTimestamp("SentTime");
                    if (timestamp != null) {
                        LocalDateTime sentTime = timestamp.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
                        message.setSentTime(sentTime);
                    }

                    // 각 메시지에 대해 로그를 기록합니다.
                    Log.d("ChatAdapter", "Message: " + message.getMessageText() + ", Sender: " + message.getSenderUserId() + ", LoggedInUser: " + loggedInUserId);

                    messages.add(message);
                }
            }
        }
        return messages;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    // 새로운 메시지를 추가하는 메서드
    public boolean addMessage(int chatId, int senderUserId, String messageText, ZonedDateTime kstTime) {
        String query = "INSERT INTO ChatMessage (ChatID, SenderUserID, MessageText, SentTime) VALUES (?, ?, ?, ?)";

        // 데이터베이스 연결을 확인하고 PreparedStatement를 사용하여 메시지를 추가합니다.
        try (Connection conn = this.connection != null && !this.connection.isClosed() ? this.connection : new DatabaseConnection().connect();
             PreparedStatement statement = conn.prepareStatement(query)) {

            // 쿼리의 각 매개변수를 설정합니다.
            statement.setInt(1, chatId);
            statement.setInt(2, senderUserId);
            statement.setString(3, messageText);

            // ZonedDateTime을 포맷팅된 문자열로 변환하여 저장합니다.
            String formattedDateTime = kstTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            statement.setString(4, formattedDateTime); // DATETIME 형식으로 저장

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                Log.i(TAG, "Message successfully added.");

                // 메시지 추가 후 로그를 출력합니다.
                Log.d("ChatAdapter", "Message: " + messageText + ", Sender: " + senderUserId + ", SentTime: " + formattedDateTime);

                // Chat 테이블의 LastMessage와 LastMessageTime을 업데이트합니다.
                String updateChatQuery = "UPDATE Chat SET LastMessage = ?, LastMessageTime = ? WHERE ChatID = ?";
                try (PreparedStatement updateChatStmt = conn.prepareStatement(updateChatQuery)) {
                    updateChatStmt.setString(1, messageText);
                    updateChatStmt.setString(2, formattedDateTime); // 같은 KST 시간 사용
                    updateChatStmt.setInt(3, chatId);
                    updateChatStmt.executeUpdate();

                    if (!conn.getAutoCommit()) {
                        conn.commit(); // 트랜잭션을 명시적으로 커밋합니다.
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
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}
