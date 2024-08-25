package com.example.hackathonproject.Chat;

public class Chat {
    // 채팅의 고유 ID
    private int chatID;

    // 채팅에 참여한 첫 번째 사용자 ID
    private int userID1;

    // 채팅에 참여한 두 번째 사용자 ID
    private int userID2;

    // 마지막으로 보낸 메시지 내용
    private String lastMessage;

    // 마지막 메시지의 시간
    private String lastMessageTime;

    // 새로운 메시지가 있는지 여부를 나타내는 플래그
    private boolean newMessage;

    // 상대방의 이름을 저장하는 필드
    private String otherUserName;

    // 관련된 교육 게시글 ID (null 가능)
    private Integer postID;

    // 관련된 강연 ID (null 가능)
    private Integer lectureID;

    // 생성자에서 chatType을 제거하여 인자 수를 맞춤
    public Chat(int chatID, int userID1, int userID2, String lastMessage, String lastMessageTime, Integer postID, Integer lectureID) {
        this.chatID = chatID;
        this.userID1 = userID1;
        this.userID2 = userID2;
        // 마지막 메시지가 null이 아닌 경우 그대로 저장, null인 경우 빈 문자열로 설정
        this.lastMessage = (lastMessage != null) ? lastMessage : "";
        // 마지막 메시지 시간이 null이 아닌 경우 그대로 저장, null인 경우 빈 문자열로 설정
        this.lastMessageTime = (lastMessageTime != null) ? lastMessageTime : "";
        this.newMessage = false; // 기본값으로 새로운 메시지 없음으로 설정
        this.postID = postID;
        this.lectureID = lectureID;
        this.otherUserName = ""; // 기본값으로 빈 문자열 설정
    }

    // Getters and Setters
    // 각 필드에 접근하기 위한 Getter 및 Setter 메서드들

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

    // 현재 사용자의 ID를 기반으로 상대방의 사용자 ID를 반환
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
