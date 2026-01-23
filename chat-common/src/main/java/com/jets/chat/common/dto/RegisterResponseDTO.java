package com.jets.chat.common.dto;

import java.io.Serializable;
public class RegisterResponseDTO implements Serializable {
    private boolean success;
    private String message;
    private Long userId;

    public RegisterResponseDTO() {

    }

    public RegisterResponseDTO(boolean success, String message, Long userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }

}
