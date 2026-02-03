package com.jets.chat.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record MessageDTO(String content, LocalDateTime time,
        boolean isSentByMe) implements Serializable {
}
