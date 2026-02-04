package com.jets.chat.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record InvitationDTO(long ownerId, String userName,
        LocalDateTime createdAt) implements Serializable {
}
