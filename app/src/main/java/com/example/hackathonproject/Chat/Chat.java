package com.example.hackathonproject.Chat;

public class Chat {
    // 채팅의 고유 ID
    private int chatID;

    // 채팅에 참여한 첫 번째 사용자 ID
    private int authorID;

    // 채팅에 참여한 두 번째 사용자 ID
    private int otherUserID;

    // 마지막으로 보낸 메시지 내용
    private String lastMessage;

    // 마지막 메시지의 시간
    private String lastMessageTime;

    // 새로운 메시지가 있는지 여부를 나타내는 플래그
    private boolean newMessage;

    // 상대방의 이름을 저장하는 필드
    private String otherUserName;

    // 관련된 교육 게시글 ID (null 가능)
    private Integer educationID;

    // 관련된 강연 ID (null 가능)
    private Integer lectureID;

    // 생성자
    public Chat(int chatID, int authorID, int otherUserID, String lastMessage, String lastMessageTime, Integer educationID, Integer lectureID) {
        this.chatID = chatID;
        this.authorID = authorID;
        this.otherUserID = otherUserID;
        this.lastMessage = (lastMessage != null) ? lastMessage : "";
        this.lastMessageTime = (lastMessageTime != null) ? lastMessageTime : "";
        this.newMessage = false; // 기본값으로 새로운 메시지 없음으로 설정
        this.educationID = educationID;
        this.lectureID = lectureID;
        this.otherUserName = ""; // 기본값으로 빈 문자열 설정
    }

    // Getters and Setters

    public int getChatID() {
        return chatID;
    }

    public int getAuthorID() {
        return authorID;
    }

    public int getOtherUserID() {
        return otherUserID;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    // 현재 사용자의 ID를 기반으로 상대방의 사용자 ID를 반환
    public int getCounterpartUserID(int currentUserId) {
        return (authorID == currentUserId) ? otherUserID : authorID;
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

    public Integer getEducationID() {
        return educationID;
    }

    public void setEducationID(Integer educationID) {
        this.educationID = educationID;
    }

    public Integer getLectureID() {
        return lectureID;
    }

    public void setLectureID(Integer lectureID) {
        this.lectureID = lectureID;
    }
}
