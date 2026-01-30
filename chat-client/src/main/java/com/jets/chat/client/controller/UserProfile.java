package com.jets.chat.client.controller;

import com.jets.chat.common.enums.UserStatus;
import javafx.scene.image.Image;
import java.time.LocalDate;

public class UserProfile {
    private final String name;
    private final String password; // Added
    private final String gender;
    private final String country;
    private final String bio;
    private final LocalDate dob;
    private final Image profileImage;
    private final UserStatus status;

    public UserProfile(String name, String password, String gender, String country, LocalDate dob,
            String bio, Image profileImage, UserStatus status) {
        this.name = name;
        this.password = password;
        this.gender = gender;
        this.country = country;
        this.dob = dob;
        this.bio = bio;
        this.profileImage = profileImage;
        this.status = status;
    }

    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public String getGender() {
        return gender;
    }
    public String getCountry() {
        return country;
    }
    public LocalDate getDob() {
        return dob;
    }
    public String getBio() {
        return bio;
    }
    public Image getProfileImage() {
        return profileImage;
    }
    public UserStatus getStatus() {
        return status;
    }
}