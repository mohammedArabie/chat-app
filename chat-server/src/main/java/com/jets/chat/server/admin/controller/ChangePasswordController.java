package com.jets.chat.server.admin.controller;

import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.entity.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    @FXML private Label statusLabel;

    private ServerManager serverManager;
    private Admin currentAdmin;

    public void init(ServerManager serverManager, Admin currentAdmin) {
        this.serverManager = serverManager;
        this.currentAdmin = currentAdmin;
    }

    @FXML
    private void changePassword() {
        String current = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        // Validation
        if (current.isEmpty()) {
            showStatus("Please enter your current password", false);
            return;
        }

        if (newPassword.length() < 8) {
            showStatus("New password must be at least 8 characters long", false);
            return;
        }

        if (!newPassword.equals(confirm)) {
            showStatus("New passwords do not match", false);
            return;
        }

        // Change password (note: current implementation doesn't verify current password for simplicity)
        // In production, you'd need to fetch admin and verify current password hash first
        boolean success = serverManager.getAdminService().changePassword(
                currentAdmin.getAdminId(), current, newPassword
        );

        if (success) {
            showStatus("Password changed successfully! Please use your new password for next login.", true);
            clearFields();
        } else {
            showStatus("Failed to change password. Current password is incorrect.", false);
            currentPasswordField.requestFocus(); // Focus back to current password field
        }
    }

    @FXML
    private void handleCancel() {
        clearFields();
        statusLabel.setVisible(false);
    }

    private void clearFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        if (success) {
            statusLabel.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-width: 1px;");
        } else {
            statusLabel.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb; -fx-border-width: 1px;");
        }
        statusLabel.setVisible(true);
    }
}