package com.example.hackathonproject.Chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private int messageId;
    private int chatId;
    private int senderUserId;
    private String messageText;
    private LocalDateTime sentTime;

    // Existing Getters and Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(int senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }

    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }

    // Method to get formatted time
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
        return sentTime.format(formatter);
    }
}
