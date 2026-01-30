package com.jets.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMain extends Application {
    private static final Logger logger = Logger.getLogger(ClientMain.class.getName());

    private static final String FXML_PATH = "/fxml/profile.fxml";
    private static final String CSS_PATH = "/styles/styles.css";
    private static final String BUNDLE_PATH = "properties files.messages";

    @Override
    public void start(Stage primaryStage) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault());

            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setResources(bundle);

            Parent root = loader.load();

            Scene scene = new Scene(root, 420, 750);
            var cssUrl = getClass().getResource(CSS_PATH);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                logger.warning("CSS file not found at: " + CSS_PATH);
            }

            primaryStage.setTitle(bundle.getString("app.title"));
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(420);
            primaryStage.show();

            logger.info("Chat Client started successfully.");

        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private void handleStartupError(Exception e) {
        logger.log(Level.SEVERE, "Fatal error: Client failed to start", e);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Could not start the application");
            alert.setContentText(
                    "Please check if the RMI Server is running.\n\nDetails: " + e.getMessage());

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Platform.exit();
                    System.exit(0);
                }
            });
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}