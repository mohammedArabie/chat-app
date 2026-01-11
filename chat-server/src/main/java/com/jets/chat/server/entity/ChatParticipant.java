package com.jets.chat.server.entity;

import java.time.LocalDateTime;

public class ChatParticipant {
    private long chatId;
    private long userId;
    private LocalDateTime joinedAt;

    public ChatParticipant() {
    }

    public ChatParticipant(long chatId, long userId, LocalDateTime joinedAt) {
        this.chatId = chatId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
