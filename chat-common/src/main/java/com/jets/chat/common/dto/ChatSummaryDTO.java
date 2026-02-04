package com.jets.chat.common.dto;

import com.jets.chat.common.enums.UserStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ChatSummaryDTO(long chatId, String chatName, String lastMessage,
        LocalDateTime lastMessageTime, String lastMessageSender,
        UserStatus status) implements Serializable {

    public String getFormattedTime() {
        if (lastMessageTime == null)
            return "";

        LocalDateTime now = LocalDateTime.now();
        if (lastMessageTime.toLocalDate().isEqual(now.toLocalDate())) {
            return lastMessageTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else {
            return lastMessageTime.format(DateTimeFormatter.ofPattern("MMM dd"));
        }
    }
}