package com.jets.chat.server.admin.controller;

import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.entity.Admin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;

public class AdminLoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private ServerManager serverManager;

    public AdminLoginController() {
        try {
            this.serverManager = ServerManager.getInstance();
        } catch (RemoteException e) {
            showError("Server unavailable. Please ensure the chat server is running.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Username cannot be empty");
            return;
        }
        if (password.isEmpty()) {
            showError("Password cannot be empty");
            return;
        }

        var adminOpt = serverManager.getAdminService().authenticate(username, password);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            // ✅ CRITICAL: Pass firstLogin flag to dashboard
            boolean mustChange = serverManager.getAdminService()
                    .mustChangePassword(admin.getAdminId());

            if (mustChange) {
                loadMustChangePassword(admin); // New Gatekeeper Screen
            } else {
                loadDashboard(admin, false); // Normal Flow
            }

        } else {
            showError("Invalid username or password");
            passwordField.clear();
        }
    }
    private void loadMustChangePassword(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/must-change-password.fxml"));
            Parent root = loader.load();

            MustChangePasswordController controller = loader.getController();
            controller.init(serverManager, admin);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ UPDATED: Accepts firstLogin flag
    private void loadDashboard(Admin admin, boolean firstLogin) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/admin-dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController dashboardController = loader.getController();
            dashboardController.init(serverManager, admin, firstLogin); // ← PASS FLAG

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat Server Admin Dashboard - " + admin.getUsername());
            stage.setWidth(1300);
            stage.setHeight(820);
            stage.centerOnScreen();
            stage.show();

            usernameField.clear();
            passwordField.clear();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(5));
        pause.setOnFinished(event -> errorLabel.setVisible(false));
        pause.play();
    }
}