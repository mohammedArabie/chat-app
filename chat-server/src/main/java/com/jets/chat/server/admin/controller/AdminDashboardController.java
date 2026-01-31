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

/**
 * Main dashboard controller - handles navigation between views
 */
public class AdminDashboardController {

    // ===== TOP BAR =====
    @FXML private Label serverStatus;          // ● dot
    @FXML private Label serverStatusLabel;     // Running / Stopped
    @FXML private Button serverControlBtn;
    @FXML private Label adminInfo;

    // ===== NAV =====
    @FXML private Button statsBtn, userMgmtBtn, addAdminBtn, pwdBtn;

    // ===== CONTENT =====
    @FXML private StackPane contentArea;

    private ServerManager serverManager;
    private Admin currentAdmin;

    // ================= INIT =================
    public void init(ServerManager serverManager, Admin admin) {
        this.serverManager = serverManager;
        this.currentAdmin = admin;

        adminInfo.setText("Admin: " + admin.getUsername());
        updateServerStatus();
        showStatistics(); // default view
    }

    // ================= SERVER UI =================
    private void updateServerStatus() {

        if (serverManager.isServerRunning()) {

            // Dot
            serverStatus.setText("●");
            serverStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 22px;");

            // Text
            serverStatusLabel.setText("Running");
            serverStatusLabel.getStyleClass().setAll("running");

            // Button
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

    // ================= START / STOP =================
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

    // ================= NAVIGATION =================
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

    // ================= LOGOUT =================
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/view/admin-login.fxml"));
            loader.load();

            Stage stage = (Stage) serverStatus.getScene().getWindow();
            stage.setScene(new Scene(loader.getRoot()));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================= VIEW LOADER =================
    private void loadView(String fxmlPath, Button activeButton) {

        statsBtn.getStyleClass().remove("selected");
        userMgmtBtn.getStyleClass().remove("selected");
        addAdminBtn.getStyleClass().remove("selected");
        pwdBtn.getStyleClass().remove("selected");

        activeButton.getStyleClass().add("selected");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof StatisticsController c) {
                c.init(serverManager);
            } else if (controller instanceof UserManagementController c) {
                c.init(serverManager);
            } else if (controller instanceof AddAdminController c) {
                c.init(serverManager, currentAdmin);
            } else if (controller instanceof ChangePasswordController c) {
                c.init(serverManager, currentAdmin);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load view", e.getMessage());
        }
    }

    // ================= ERROR =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
