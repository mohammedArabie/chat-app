package com.jets.chat.server.entity;

import java.sql.Timestamp;

public class ChatParticipant {
    private long chatId;
    private long userId;
    private Timestamp joinedAt;

    public ChatParticipant() {
    }

    public ChatParticipant(long chatId, long userId, Timestamp joinedAt) {
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

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}
