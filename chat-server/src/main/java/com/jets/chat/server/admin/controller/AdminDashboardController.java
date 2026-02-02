package com.jets.chat.server.admin.controller;

import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.entity.Admin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {
    @FXML
    private Label serverStatus, serverStatusLabel, adminInfo;
    @FXML
    private Button serverControlBtn, statsBtn, userMgmtBtn, addAdminBtn, pwdBtn, announceBtn;
    @FXML
    private StackPane contentArea;

    private ServerManager serverManager;
    private Admin currentAdmin;

    // ✅ UPDATED: Accepts firstLogin flag
    public void init(ServerManager serverManager, Admin admin, boolean firstLogin) {
        this.serverManager = serverManager;
        this.currentAdmin = admin;
        adminInfo.setText("Admin: " + admin.getUsername());
        updateServerStatus();

        // ✅ CRITICAL: Auto-navigate to change password on first login
        if (firstLogin) {
            showChangePassword(); // ← AUTO-REDIRECT AFTER DASHBOARD LOADS
        } else {
            showStatistics(); // normal flow
        }
    }

    private void updateServerStatus() {
        if (serverManager.isServerRunning()) {
            serverStatus.setText("●");
            serverStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 22px;");
            serverStatusLabel.setText("Running");
            serverStatusLabel.getStyleClass().setAll("running");
            serverControlBtn.setText("Stop Server");
            serverControlBtn.getStyleClass().setAll("server-running");
        } else {
            serverStatus.setText("●");
            serverStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 22px;");
            serverStatusLabel.setText("Stopped");
            serverStatusLabel.getStyleClass().setAll("stopped");
            serverControlBtn.setText("Start Server");
            serverControlBtn.getStyleClass().setAll("server-stopped");
        }
    }

    @FXML
    private void toggleServer() {
        try {
            if (serverManager.isServerRunning()) {
                serverManager.stopServer();
            } else {
                serverManager.startServer();
            }
            updateServerStatus();
        } catch (Exception e) {
            showError("Server operation failed", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showStatistics() {
        loadView("/view/statistics-view.fxml", statsBtn);
    }
    @FXML
    private void showUserManagement() {
        loadView("/view/user-management.fxml", userMgmtBtn);
    }
    @FXML
    private void showAddAdmin() {
        loadView("/view/add-admin.fxml", addAdminBtn);
    }
    @FXML
    private void showChangePassword() {
        loadView("/view/change-password.fxml", pwdBtn);
    }
    @FXML
    private void showAnnouncement() {
        loadView("/view/announcement-view.fxml", announceBtn);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin-login.fxml"));
            loader.load();
            Stage stage = (Stage) serverStatus.getScene().getWindow();
            stage.setScene(new Scene(loader.getRoot()));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, Button activeButton) {
        statsBtn.getStyleClass().remove("selected");
        userMgmtBtn.getStyleClass().remove("selected");
        addAdminBtn.getStyleClass().remove("selected");
        pwdBtn.getStyleClass().remove("selected");
        announceBtn.getStyleClass().remove("selected");
        activeButton.getStyleClass().add("selected");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            Object controller = loader.getController();

            if (controller instanceof StatisticsController c)
                c.init(serverManager);
            else if (controller instanceof UserManagementController c)
                c.init(serverManager);
            else if (controller instanceof AddAdminController c)
                c.init(serverManager, currentAdmin);
            else if (controller instanceof ChangePasswordController c)
                c.init(serverManager, currentAdmin);
            else if (controller instanceof AnnouncementController c)
                c.init(serverManager);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load view", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}