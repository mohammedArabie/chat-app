package com.jets.chat.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record MessageDTO(long chatId, String content, LocalDateTime time, boolean isSentByMe,
        String messageType, // "TEXT" or "FILE"
        FileDTO fileMetadata // null for text messages, populated for file messages
        , String senderName) implements Serializable {

    public MessageDTO(long chatId, String content, LocalDateTime time, boolean isSentByMe,
            String senderName) {
        this(chatId, content, time, isSentByMe, "TEXT", null, senderName);
    }

    public MessageDTO(long chatId, String content, LocalDateTime time, boolean isSentByMe,
            FileDTO fileMetadata, String senderName) {
        this(chatId, content, time, isSentByMe, "FILE", fileMetadata, senderName);
    }

    public boolean isFileMessage() {
        return "FILE".equals(messageType);
    }
}