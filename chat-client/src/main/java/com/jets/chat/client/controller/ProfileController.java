package com.jets.chat.client.controller;

import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.Gender;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import java.io.File;

public class ProfileController {
    @FXML
    private ProfileAvatarController avatarController;
    @FXML
    private ProfileStatusController statusController;
    @FXML
    private ProfileInfoController infoController;

    private final UserService userService = new UserService();
    private UserDTO currentUser;

    @FXML
    public void initialize() {
        if (infoController != null)
            infoController.setMainController(this);
        if (avatarController != null)
            avatarController.setMainController(this);
        loadUserDataFromServer(1);
    }

    private void loadUserDataFromServer(long userId) {
        currentUser = userService.getUserById(userId);
        UserStatus currentStatus = userService.getUserStatus(userId);
        if (currentUser != null) {
            fillUiWithData(currentUser, currentStatus);
        }
    }

    public void saveProfileNameOnly(String newName) {
        if (currentUser == null)
            return;
        currentUser.setDisplayName(newName);
        userService.saveUserProfile(currentUser);
    }

    public void saveProfilePicturePathOnly(String path) {
        if (currentUser == null)
            return;
        currentUser.setPicturePath(path);
        userService.saveUserProfile(currentUser);
    }

    private void fillUiWithData(UserDTO user, UserStatus status) {
        avatarController.setName(user.getDisplayName());

        if (user.getPicturePath() != null && !user.getPicturePath().isEmpty()) {
            File imageFile = new File(user.getPicturePath());
            if (imageFile.exists()) {
                avatarController.setImage(new Image(imageFile.toURI().toString()));
            }
        }

        if (statusController != null)
            statusController.setStatus(status);
        infoController.setEmail(user.getEmail());
        infoController.setPhone(user.getPhoneNumber());
        infoController.setCountry(user.getCountry());
        infoController.setBio(user.getBio());

        if (user.getGender() != null)
            infoController.setGender(user.getGender().name());

        if (user.getPasswordHash() != null) {
            String decryptedPassword = PasswordUtil.decrypt(user.getPasswordHash());
            infoController.setPassword(decryptedPassword);
        }

        if (user.getDateOfBirth() != null) {
            infoController.setDob(new java.sql.Date(user.getDateOfBirth().getTime()).toLocalDate());
        }
    }

    public void saveProfile() {
        if (currentUser == null)
            return;

        currentUser.setDisplayName(avatarController.getName());
        if (infoController.getDob() != null) {
            currentUser.setDateOfBirth(java.sql.Date.valueOf(infoController.getDob()));
        }

        if (infoController.getGender() != null) {
            try {
                currentUser.setGender(Gender.valueOf(infoController.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
            }
        }

        currentUser.setCountry(infoController.getCountry());
        currentUser.setBio(infoController.getBio());

        if (userService.saveUserProfile(currentUser)) {
            String newPass = infoController.getPassword();
            if (!newPass.isEmpty()) {
                String encryptedPassword = PasswordUtil.encrypt(newPass);
                userService.updatePassword(currentUser.getUserId(), encryptedPassword);
            }
            infoController.resetEditMode();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Saved with AES encryption!");
        }
    }

    public void restoreData() {
        if (currentUser != null) {
            UserStatus s = userService.getUserStatus(currentUser.getUserId());
            fillUiWithData(currentUser, s);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}