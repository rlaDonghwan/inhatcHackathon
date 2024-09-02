package com.example.hackathonproject.Chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private int messageId;  // 메시지 ID
    private int chatId;  // 채팅방 ID
    private int senderUserId;  // 메시지 전송자 ID
    private String messageText;  // 메시지 내용
    private LocalDateTime sentTime;  // 메시지 전송 시간
    private int authorID;  // 메시지 작성자 ID

    public int getAuthorID() {
        return authorID;
    }

    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    // 기존의 Getters와 Setters
    public int getMessageId() {
        return messageId;  // 메시지 ID를 반환
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;  // 메시지 ID를 설정
    }

    public int getChatId() {
        return chatId;  // 채팅방 ID를 반환
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;  // 채팅방 ID를 설정
    }

    public int getSenderUserId() {
        return senderUserId;  // 전송자 ID를 반환
    }

    public void setSenderUserId(int senderUserId) {
        this.senderUserId = senderUserId;  // 전송자 ID를 설정
    }

    public String getMessageText() {
        return messageText;  // 메시지 내용을 반환
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;  // 메시지 내용을 설정
    }

    public LocalDateTime getSentTime() {
        return sentTime;  // 메시지 전송 시간을 반환
    }

    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;  // 메시지 전송 시간을 설정
    }

    // 메시지 전송 시간을 포맷하여 반환하는 메서드
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");  // 포맷 설정: "오후 5:20" 형식
        return sentTime.format(formatter);  // 포맷된 시간 문자열을 반환
    }
}
