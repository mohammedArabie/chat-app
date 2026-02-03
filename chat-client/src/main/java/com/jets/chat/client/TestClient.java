// package com.jets.chat.client;
//
// import com.jets.chat.client.callback.ClientAnnouncementCallback;
// import com.jets.chat.client.util.ClientManager;
// import com.jets.chat.common.rmi.AnnouncementCallback;
// import javafx.application.Application;
// import javafx.geometry.Insets;
// import javafx.scene.Scene;
// import javafx.scene.control.Alert;
// import javafx.scene.control.Button;
// import javafx.scene.control.Label;
// import javafx.scene.control.TextField;
// import javafx.scene.layout.VBox;
// import javafx.stage.Stage;
//
// import java.util.UUID;
//
// public class TestClient extends Application {
// private Stage primaryStage;
// private AnnouncementCallback callback;
// private String sessionId;
//
// @Override
// public void start(Stage primaryStage) {
// this.primaryStage = primaryStage;
// primaryStage.setTitle("BootTest Client - Announcement Tester");
//
// // Simple login form
// TextField usernameField = new TextField();
// usernameField.setPromptText("Enter any username (e.g., testuser)");
//
// Button loginBtn = new Button("LOGIN → Receive Announcements");
// loginBtn.setStyle(
// "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px;
// -fx-padding: 12px 25px;");
// loginBtn.setOnAction(e -> handleLogin(usernameField.getText().trim()));
//
// VBox root = new VBox(20);
// root.setPadding(new Insets(40));
// root.setStyle("-fx-background-color: #f8f9fa;");
// root.getChildren().addAll(new Label("BootTest Client\n(For testing server
// announcements)"),
// usernameField, loginBtn,
// new Label("After login, announcements from admin will pop up here!"));
// root.setStyle("-fx-alignment: center; -fx-spacing: 20px; -fx-padding:
// 40px;");
//
// primaryStage.setScene(new Scene(root, 400, 300));
// primaryStage.show();
// }
//
// private void handleLogin(String username) {
// if (username.isEmpty()) {
// showError("Username cannot be empty");
// return;
// }
//
// try {
// // ✅ SIMULATE LOGIN: Generate session ID (real app would authenticate first)
// this.sessionId = "session-" + UUID.randomUUID().toString().substring(0, 8);
//
// // ✅ REGISTER FOR ANNOUNCEMENTS
// this.callback = new ClientAnnouncementCallback(primaryStage);
// ClientManager.getInstance().getRemoteAnnouncementService().registerCallback(sessionId,
// callback);
//
// // Show success
// Alert alert = new Alert(Alert.AlertType.INFORMATION);
// alert.setTitle("✓ Connected");
// alert.setHeaderText(null);
// alert.setContentText("Logged in as: " + username + "\nSession ID: " +
// sessionId
// + "\n\nWaiting for announcements from admin...");
// alert.showAndWait();
//
// } catch (Exception e) {
// e.printStackTrace();
// showError("Failed to connect: " + e.getMessage());
// }
// }
//
// private void showError(String message) {
// Alert alert = new Alert(Alert.AlertType.ERROR);
// alert.setTitle("Error");
// alert.setHeaderText(null);
// alert.setContentText(message);
// alert.showAndWait();
// }
//
// @Override
// public void stop() {
// // Cleanup on exit
// if (sessionId != null && callback != null) {
// try {
// ClientManager.getInstance().getRemoteAnnouncementService()
// .unregisterCallback(sessionId);
// java.rmi.server.UnicastRemoteObject.unexportObject(callback, true);
// } catch (Exception e) {
// // Ignore cleanup errors
// }
// }
// }
//
// public static void main(String[] args) {
// launch(args);
// }
// }