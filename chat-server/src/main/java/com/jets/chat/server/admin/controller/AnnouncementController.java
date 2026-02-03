package com.jets.chat.server.admin.controller;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.server.context.ServerManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class AnnouncementController {
    @FXML
    private TextArea announcementText;
    @FXML
    private ComboBox<String> fontCombo, colorCombo;
    @FXML
    private CheckBox boldCheck, italicCheck;
    @FXML
    private Label previewText;
    @FXML
    private VBox root;

    private ServerManager serverManager;

    private final Map<String, String> colorMap = new HashMap<>() {
        {
            put("Black", "#000000");
            put("Dark Gray", "#2c3e50");
            put("Blue", "#3498db");
            put("Green", "#27ae60");
            put("Red", "#e74c3c");
            put("Purple", "#9b59b6");
            put("Orange", "#f39c12");
            put("Teal", "#1abc9c");
        }
    };

    public void init(ServerManager serverManager) {
        this.serverManager = serverManager;
        setupForm();
    }

    private void setupForm() {
        // Font options
        fontCombo.getItems().addAll("Arial", "Helvetica", "Times New Roman", "Courier New",
                "Verdana");
        fontCombo.setValue("Arial");

        colorCombo.getItems().addAll(colorMap.keySet());
        colorCombo.setValue("Black"); // Default to Black

        // Live preview updates
        announcementText.textProperty().addListener((obs, old, newVal) -> updatePreview());
        fontCombo.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        colorCombo.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        boldCheck.selectedProperty().addListener((obs, old, newVal) -> updatePreview());
        italicCheck.selectedProperty().addListener((obs, old, newVal) -> updatePreview());

        updatePreview();
    }

    private void updatePreview() {
        String text = announcementText.getText().trim();
        if (text.isEmpty()) {
            previewText.setText("Your announcement will appear here");
            previewText.setStyle("-fx-text-fill: #7f8c8d;");
            return;
        }

        // CONVERT HUMAN NAME → HEX FOR STYLING
        String hexColor = colorMap.get(colorCombo.getValue());
        previewText.setText(text);
        previewText.setStyle(String.format(
                "-fx-font-family: '%s'; -fx-font-size: 17px; -fx-text-fill: %s; %s %s",
                fontCombo.getValue(), hexColor, // Use hex code for actual styling
                boldCheck.isSelected() ? "-fx-font-weight: bold;" : "",
                italicCheck.isSelected() ? "-fx-font-style: italic;" : ""));
    }

    @FXML
    private void sendAnnouncement() {
        String content = announcementText.getText().trim();
        if (content.isEmpty()) {
            showError("Please enter announcement content");
            return;
        }

        try {
            AnnouncementDTO dto = new AnnouncementDTO();
            dto.setContent(content);
            dto.setFontStyle(fontCombo.getValue());
            dto.setFontColor(colorMap.get(colorCombo.getValue())); // Convert to hex
            dto.setBold(boldCheck.isSelected());
            dto.setItalic(italicCheck.isSelected());

            // This will save to DB AND broadcast to online users
            serverManager.getAnnouncementService().createAnnouncement(dto);

            // Show success message
            showSuccess(
                    "✓ Announcement sent successfully!\nIt has been broadcast to all online users.");

            // Clear form
            announcementText.clear();
            boldCheck.setSelected(false);
            italicCheck.setSelected(false);
            updatePreview();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to send announcement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        announcementText.clear();
        boldCheck.setSelected(false);
        italicCheck.setSelected(false);
        updatePreview();
    }

    @FXML
    private void handleLogout() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/view/admin-login.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) this.root.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}