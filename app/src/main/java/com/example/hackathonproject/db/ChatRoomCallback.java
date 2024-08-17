package com.example.hackathonproject.db;

public interface ChatRoomCallback {
    void onSuccess(int chatId);
    void onError(Exception e);
}
