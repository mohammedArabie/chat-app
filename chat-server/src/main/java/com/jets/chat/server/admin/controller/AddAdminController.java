package com.jets.chat.server.admin.controller;

import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.entity.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddAdminController {
    @FXML private TextField usernameField, passwordField;
    @FXML private Label statusLabel;
    @FXML private Button createBtn, regenerateBtn;

    private ServerManager serverManager;
    private Admin currentAdmin;

    public void init(ServerManager serverManager, Admin currentAdmin) {
        this.serverManager = serverManager;
        this.currentAdmin = currentAdmin;
        regeneratePassword();
    }

    @FXML
    private void regeneratePassword() {
        passwordField.setText(serverManager.getAdminService().generateRandomPassword());
        regenerateBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 10px 18px; -fx-background-radius: 8px;");
    }

    @FXML
    private void createAdmin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showStatus("Username cannot be empty", false);
            return;
        }

        if (username.length() < 3) {
            showStatus("Username must be at least 3 characters", false);
            return;
        }

        if (serverManager.getAdminService().createAdmin(username, password)) {
            String message = String.format(
                    "✓ Admin '%s' created successfully!\nTemporary password: %s\nAdmin should change password after first login.",
                    username, password
            );
            showStatus(message, true);
            usernameField.clear();
            regeneratePassword();
        } else {
            showStatus("Failed to create admin. Username might already exist.", false);
        }
    }

    @FXML
    private void handleCancel() {
        usernameField.clear();
        regeneratePassword();
        statusLabel.setVisible(false);
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        if (success) {
            statusLabel.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; " +
                    "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 12px; -fx-font-size: 15px;");
        } else {
            statusLabel.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb; " +
                    "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 12px; -fx-font-size: 15px;");
        }
        statusLabel.setVisible(true);
    }
}