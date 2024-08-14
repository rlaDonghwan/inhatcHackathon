package com.example.hackathonproject.Chat;

public class ChatItem {
    private String title;
    private String lastMessage;
    private String timestamp;
    private boolean newMessage;

    public ChatItem(String title, String lastMessage, String timestamp, boolean newMessage) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.newMessage = newMessage;
    }

    public String getTitle() {
        return title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isNewMessage() {
        return newMessage;
    }
}
