package com.jets.chat.server.admin;

import com.jets.chat.server.context.ServerManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AdminApplication extends Application {
    private ServerManager serverManager;

    @Override
    public void init() throws Exception {
        printSecurityBanner();

        // Initialize server manager (creates RMI registry ONCE)
        serverManager = ServerManager.getInstance();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Load login UI FIRST (don't auto-start server yet)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin-login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets()
                .add(getClass().getResource("/style/admin-style.css").toExternalForm());

        stage.setTitle("Chat Server Admin Login");
        stage.setScene(scene);
        stage.setWidth(850);
        stage.setHeight(650);
        stage.centerOnScreen();

        // Graceful shutdown hook
        stage.setOnCloseRequest(event -> {
            event.consume();
            confirmExit(stage);
        });

        stage.show();
        System.out.println("💡 Admin login ready. Server will start after successful login.");
    }

    private void confirmExit(Stage stage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Exit Admin Dashboard");
        confirm.setHeaderText("Stop server and exit?");
        confirm.setContentText(
                "Do you want to stop the chat server and close the admin dashboard?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (serverManager.isServerRunning()) {
                        serverManager.stopServer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private void printSecurityBanner() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}