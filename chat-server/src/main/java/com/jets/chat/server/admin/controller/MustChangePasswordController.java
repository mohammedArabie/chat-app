package com.jets.chat.server.admin.controller;

import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.entity.Admin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import java.io.IOException;
public class MustChangePasswordController {
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private ServerManager serverManager;
    private Admin currentAdmin;

    public void init(ServerManager serverManager, Admin admin) {
        this.serverManager = serverManager;
        this.currentAdmin = admin;
    }

    // Matches onAction="#changePassword" in your FXML
    @FXML
    private void changePassword() {
        String current = currentPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (newPwd.length() < 8) {
            showStatus("Password must be at least 8 characters", false);
            return;
        }

        if (!newPwd.equals(confirm)) {
            showStatus("New passwords do not match", false);
            return;
        }

        boolean success = serverManager.getAdminService().changePassword(
                currentAdmin.getAdminId(), current, newPwd
        );

        if (success) {
            navigateToDashboard();
        } else {
            showStatus("Current password incorrect", false);
        }
    }

    // Matches onAction="#handleCancel" in your FXML
    @FXML
    private void handleCancel() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        statusLabel.setVisible(false);
    }

    private void showStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        if (isSuccess) {
            statusLabel.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da;");
        }
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin-dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController dashboardController = loader.getController();
            // Pass 'false' for firstLogin flag
            dashboardController.init(serverManager, currentAdmin, false);

            Stage stage = (Stage) currentPasswordField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
