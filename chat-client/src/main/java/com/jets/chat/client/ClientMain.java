package com.jets.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ClientMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Load FXML
            URL fxmlUrl = getClass().getResource("/fxml/RegisterView.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML file not found at /fxml/RegisterView.fxml");
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 900, 600);

            // Load CSS (optional - won't crash if missing)
            URL cssUrl = getClass().getResource("/styles/register-style.css");
            if (cssUrl == null) {
                // Try alternative name
                cssUrl = getClass().getResource("/styles/register-styles.css");
            }

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS loaded: " + cssUrl);
            } else {
                System.out.println("WARNING: CSS file not found, using default styling");
            }

            stage.setTitle("register");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}