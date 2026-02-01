package com.jets.chat.common.dto;

import java.io.Serializable;

public class LoginResult implements Serializable {

    private boolean success;
    private String errorMessage;
    private UserDTO userDto;
    private String sessionId;

    public LoginResult(UserDTO userDto, String sessionId) {
        this.success = true;
        this.errorMessage = null;
        this.userDto = userDto;
        this.sessionId = sessionId;
    }

    public LoginResult(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.userDto = null;
        this.sessionId = null;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public UserDTO getUserDto() {
        return userDto;
    }

    public String getSessionId() {
        return sessionId;
    }
}