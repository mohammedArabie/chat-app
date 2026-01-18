package com.jets.chat.server.entity;

import com.jets.chat.common.enums.ChatType;

import java.sql.Timestamp;

public class Chat {
    private long chatId;
    private ChatType chatType; // PRIVATE or GROUP
    private Timestamp createdAt;

    public Chat() {
    }

    public Chat(long chatId, ChatType chatType, Timestamp createdAt) {
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
