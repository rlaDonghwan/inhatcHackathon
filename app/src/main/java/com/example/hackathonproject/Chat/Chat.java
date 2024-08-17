package com.example.hackathonproject.Chat;

public class Chat {
    private int chatID;
    private int userID1;
    private int userID2;
    private String lastMessage;
    private String lastMessageTime;
    private String chatType;
    private boolean newMessage;  // 새로운 메시지가 있는지 여부를 나타내는 필드 추가
    private String otherUserName; // 상대방의 이름을 저장하는 필드 추가

    public Chat(int chatID, int userID1, int userID2, String lastMessage, String lastMessageTime, String chatType) {
        this.chatID = chatID;
        this.userID1 = userID1;
        this.userID2 = userID2;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.chatType = chatType;
        this.newMessage = false;  // 기본값으로 새로운 메시지가 없다고 설정
    }

    public int getChatID() {
        return chatID;
    }

    public int getUserID1() {
        return userID1;
    }

    public int getUserID2() {
        return userID2;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public String getChatType() {
        return chatType;
    }

    public int getOtherUserID(int currentUserId) {
        return (userID1 == currentUserId) ? userID2 : userID1;
    }

    public boolean isNewMessage() {
        return newMessage;
    }

    public void setNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }
}
