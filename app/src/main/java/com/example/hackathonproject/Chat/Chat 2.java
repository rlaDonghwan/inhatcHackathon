package com.example.hackathonproject.Chat;

public class Chat {
    private int chatID;
    private int userID1;
    private int userID2;
    private String lastMessage;
    private String lastMessageTime;
    private boolean newMessage;
    private String otherUserName;  // 상대방 이름 필드
    private Integer postID;        // 관련된 교육 게시글 ID (null 가능)
    private Integer lectureID;     // 관련된 강연 ID (null 가능)

    // 생성자에서 chatType을 제거하여 인자 수를 맞춤
    public Chat(int chatID, int userID1, int userID2, String lastMessage, String lastMessageTime, Integer postID, Integer lectureID) {
        this.chatID = chatID;
        this.userID1 = userID1;
        this.userID2 = userID2;
        this.lastMessage = (lastMessage != null) ? lastMessage : "";
        this.lastMessageTime = (lastMessageTime != null) ? lastMessageTime : "";
        this.newMessage = false;
        this.postID = postID;
        this.lectureID = lectureID;
        this.otherUserName = ""; // 기본값 설정
    }

    // Getters and Setters
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

    public Integer getPostID() {
        return postID;
    }

    public void setPostID(Integer postID) {
        this.postID = postID;
    }

    public Integer getLectureID() {
        return lectureID;
    }

    public void setLectureID(Integer lectureID) {
        this.lectureID = lectureID;
    }
}
