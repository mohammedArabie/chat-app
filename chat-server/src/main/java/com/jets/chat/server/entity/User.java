package com.jets.chat.server.entity;

import com.jets.chat.common.enums.Gender;

import java.sql.Timestamp;
import java.util.Date;

public class User {
    private long userId;
    private String phoneNumber;
    private String displayName;
    private String email;
    private String passwordHash;
    private Gender gender;
    private String country;
    private Date dateOfBirth;
    private String bio;
    private String picturePath;
    private Timestamp createdAt;
    private boolean chatbotEnabled;

    public User() {
    }

    public User(long userId, String phoneNumber, String displayName, String email,
            String passwordHash, Gender gender, String country, Date dateOfBirth, String bio,
            String picturePath, Timestamp createdAt, boolean chatbotEnabled) {

        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.gender = gender;
        this.country = country;
        this.dateOfBirth = dateOfBirth;
        this.bio = bio;
        this.picturePath = picturePath;
        this.createdAt = createdAt;
        this.chatbotEnabled = chatbotEnabled;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isChatbotEnabled() {
        return chatbotEnabled;
    }

    public void setChatbotEnabled(boolean chatbotEnabled) {
        this.chatbotEnabled = chatbotEnabled;
    }
}
