package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.util.ClientManager;
import com.jets.chat.client.util.SessionManager;
import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.Arrays;

public class SettingsPaneController {

    @FXML
    private TextField displayNameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<UserStatus> statusComboBox;
    @FXML
    private CheckBox systemNotificationCheckBox;
    @FXML
    private ImageView userImageView;

    private ChatViewModel viewModel;
    private File selectedImageFile;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        if (systemNotificationCheckBox != null
                && viewModel.enableSystemNotificationsProperty() != null) {
            systemNotificationCheckBox.selectedProperty()
                    .bindBidirectional(viewModel.enableSystemNotificationsProperty());
        }

        if (statusComboBox != null) {
            statusComboBox.getItems().setAll(Arrays.asList(UserStatus.values()));

            statusComboBox.setCellFactory(lv -> new ListCell<UserStatus>() {
                @Override
                protected void updateItem(UserStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: #2a2a2a;");
                    } else {
                        setText(item.toString());
                        setStyle(
                                "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-padding: 8 12;");
                    }
                }
            });
        }

        loadUserProfile();
    }

    @FXML
    private void onChangePictureClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(userImageView.getScene().getWindow());
        if (file != null) {
            this.selectedImageFile = file;
            userImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void onSaveProfileClicked() {
        String displayName = displayNameField.getText().trim();
        String email = emailField.getText().trim();
        UserStatus status = statusComboBox.getValue();

        if (displayName.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name and Email are required.");
            return;
        }

        new Thread(() -> {
            try {
                long userId = SessionManager.getUserId();

                if (selectedImageFile != null) {
                    byte[] bytes = Files.readAllBytes(selectedImageFile.toPath());
                    // ClientManager.getInstance().getRemoteUserService()
                    // .updateUserPicture(userId, bytes, selectedImageFile.getName());
                }

                UserDTO updated = ClientManager.getInstance().getRemoteUserService()
                        .updateUserProfile(userId, displayName, email);

                if (updated != null) {
                    ClientManager.getInstance().getRemoteUserService().updateStatus(userId, status);
                    SessionManager.saveDisplayName(displayName);

                    // Update view model to reflect changes in sidebar
                    viewModel.updateCurrentUserStatus(status);
                    viewModel.updateCurrentUserDisplayName(displayName);

                    Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Profile Updated!"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error",
                        "Save failed: " + e.getMessage()));
            }
        }).start();
    }

    private void loadUserProfile() {
        new Thread(() -> {
            try {
                UserDTO user = ClientManager.getInstance().getRemoteUserService()
                        .getUserById(SessionManager.getUserId());
                if (user != null) {
                    Platform.runLater(() -> {
                        displayNameField.setText(user.getDisplayName());
                        emailField.setText(user.getEmail());
                        statusComboBox.setValue(user.getStatus());
                        loadProfilePicture(user.getPicturePath());
                    });
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadProfilePicture(String path) {
        if (path != null && !path.isEmpty()) {
            File f = new File(path);
            if (f.exists()) {
                userImageView.setImage(new Image(f.toURI().toString()));
                return;
            }
        }
        try {
            userImageView.setImage(
                    new Image(getClass().getResourceAsStream("images/default_avatar.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCloseClicked() {
        if (viewModel != null)
            viewModel.closeSettingsPane();
    }

    @FXML
    private void onLogoutClicked() {
        if (viewModel != null)
            viewModel.logout();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}