package com.jets.chat.common.dto;

import java.io.Serializable;

public record UserDTO(String name, String status, String lastMsg,
        String color) implements Serializable {
}
