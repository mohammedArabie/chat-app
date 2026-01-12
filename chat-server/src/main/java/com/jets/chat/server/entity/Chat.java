package com.jets.chat.server.entity;

import java.sql.Timestamp;

public class Chat {
    private long chatId;
    private com.jets.chat.common.entity.ChatType chatType; // PRIVATE or GROUP
    private Timestamp createdAt;

    public Chat() {
    }

    public Chat(long chatId, com.jets.chat.common.entity.ChatType chatType, Timestamp createdAt) {
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

    public com.jets.chat.common.entity.ChatType getChatType() {
        return chatType;
    }

    public void setChatType(com.jets.chat.common.entity.ChatType chatType) {
        this.chatType = chatType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
