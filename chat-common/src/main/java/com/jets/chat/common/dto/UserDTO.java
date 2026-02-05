package com.jets.chat.common.dto;

import com.jets.chat.common.enums.UserStatus;

import java.io.Serializable;

public class UserDTO implements Serializable {

    private long id;
    private String displayName;
    private UserStatus status;
    private String email;
    private String phoneNumber;
    private String picturePath;

    public UserDTO(long id, String displayName, UserStatus status, String email,
            String phoneNumber) {
        this.id = id;
        this.displayName = displayName;
        this.status = status;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public UserDTO(long id, String displayName, UserStatus status, String email, String phoneNumber,
            String picturePath) {
        this.id = id;
        this.displayName = displayName;
        this.status = status;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.picturePath = picturePath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

}