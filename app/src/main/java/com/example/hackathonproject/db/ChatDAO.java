package com.example.hackathonproject.db;

import com.example.hackathonproject.Chat.Chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    private Connection connection;

    public ChatDAO(Connection connection) {
        this.connection = connection;
    }

    // 특정 사용자가 속한 모든 채팅방을 가져오는 메서드 수정
    public List<Chat> getAllChatsForUser(int userId) {
        List<Chat> chatList = new ArrayList<>();
        try {
            // 채팅방 정보와 상대방 이름을 가져오는 쿼리
            String query = "SELECT c.*, u1.Name AS UserName1, u2.Name AS UserName2 " +
                    "FROM Chat c " +
                    "JOIN User u1 ON c.UserID1 = u1.UserID " +
                    "JOIN User u2 ON c.UserID2 = u2.UserID " +
                    "WHERE c.UserID1 = ? OR c.UserID2 = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, userId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int chatId = resultSet.getInt("ChatID");
                int userId1 = resultSet.getInt("UserID1");
                int userId2 = resultSet.getInt("UserID2");
                String lastMessage = resultSet.getString("LastMessage");
                String lastMessageTime = resultSet.getString("LastMessageTime");
                String chatType = resultSet.getString("ChatType");

                // 상대방 이름 가져오기
                String otherUserName;
                if (userId == userId1) {
                    otherUserName = resultSet.getString("UserName2");
                } else {
                    otherUserName = resultSet.getString("UserName1");
                }

                Chat chat = new Chat(chatId, userId1, userId2, lastMessage, lastMessageTime, chatType);
                chat.setOtherUserName(otherUserName);  // 상대방 이름 설정
                chatList.add(chat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chatList;
    }

    // 채팅방을 가져오거나 새로 생성하는 메서드
    public void getOrCreateChatRoom(int userId1, int userId2, String chatType, ChatRoomCallback callback) {
        new Thread(() -> {
            try {
                // 기존 채팅방이 있는지 확인
                String query = "SELECT ChatID FROM Chat WHERE ((UserID1 = ? AND UserID2 = ?) OR (UserID1 = ? AND UserID2 = ?)) AND ChatType = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, userId1);
                statement.setInt(2, userId2);
                statement.setInt(3, userId2);
                statement.setInt(4, userId1);
                statement.setString(5, chatType);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int chatId = resultSet.getInt("ChatID");
                    callback.onSuccess(chatId);
                } else {
                    // 채팅방이 없으면 새로 생성
                    query = "INSERT INTO Chat (UserID1, UserID2, ChatType) VALUES (?, ?, ?)";
                    statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    statement.setInt(1, userId1);
                    statement.setInt(2, userId2);
                    statement.setString(3, chatType);
                    statement.executeUpdate();

                    resultSet = statement.getGeneratedKeys();
                    if (resultSet.next()) {
                        int chatId = resultSet.getInt(1); // 새로 생성된 ChatID 반환
                        callback.onSuccess(chatId);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                callback.onError(e);
            }
        }).start();
    }

    // ChatRoomCallback 인터페이스 정의
    public interface ChatRoomCallback {
        void onSuccess(int chatId);
        void onError(Exception e);
    }
}
