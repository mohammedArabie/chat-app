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

/**
 * Admin login screen controller
 * Handles authentication and navigation to dashboard
 */
public class AdminLoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private ServerManager serverManager;

    public AdminLoginController() {
        try {
            // Get singleton instance - will fail if server not started
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

        // Validation
        if (username.isEmpty()) {
            showError("Username cannot be empty");
            return;
        }

        if (password.isEmpty()) {
            showError("Password cannot be empty");
            return;
        }

        // Authenticate using SHA-256 hashing
        var adminOpt = serverManager.getAdminService().authenticate(username, password);

        if (adminOpt.isPresent()) {
            // Successful login - load dashboard
            loadDashboard(adminOpt.get());
        } else {
            // Failed login
            showError("Invalid username or password");
            passwordField.clear(); // Clear password field for security
        }
    }

    private void loadDashboard(Admin admin) {
        try {
            // Load dashboard FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/admin-dashboard.fxml")
            );
            Parent root = loader.load();

            // Initialize dashboard controller with dependencies
            AdminDashboardController dashboardController = loader.getController();
            dashboardController.init(serverManager, admin);

            // Switch scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat Server Admin Dashboard - " + admin.getUsername());
            stage.setWidth(1300);
            stage.setHeight(820);
            stage.centerOnScreen();
            stage.show();

            // Clear credentials from memory
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

        // Auto-hide after 5 seconds
        errorLabel.getScene().getRoot().getScene().getWindow()
                .getScene()
                .getRoot()
                .requestFocus(); // Ensure focus for auto-hide timer

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(5)
        );
        pause.setOnFinished(event -> errorLabel.setVisible(false));
        pause.play();
    }
}