package com.jets.chat.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record MessageDTO(String content, LocalDateTime time, boolean isSentByMe, String senderName,
        long chatId) implements Serializable {
}
