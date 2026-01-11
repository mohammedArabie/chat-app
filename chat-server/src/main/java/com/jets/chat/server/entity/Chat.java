package com.jets.chat.server.entity;

import java.time.LocalDateTime;

public class Chat {
    private long chatId;
    private ChatType chatType;   // PRIVATE or GROUP
    private LocalDateTime createdAt;

    public Chat() {
    }

    public Chat(long chatId, ChatType chatType, LocalDateTime createdAt) {
        this.chatId = chatId;
        this.chatType = chatType;
        this.createdAt = createdAt;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
