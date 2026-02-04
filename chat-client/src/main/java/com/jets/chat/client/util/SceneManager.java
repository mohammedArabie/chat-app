package com.jets.chat.client.util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static final int DEFAULT_WIDTH = 1440;
    private static final int DEFAULT_HEIGHT = 720;
    private static final String LOGIN_VIEW = "/fxml/LoginView.fxml";
    private static final String REGISTER_VIEW = "/fxml/RegisterView.fxml";
    private static final String MAIN_VIEW = "/fxml/MainLayout.fxml";
    private static SceneManager instance;
    private Stage primaryStage;
    private Scene scene;

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setMinWidth(800);
        this.primaryStage.setMinHeight(500);
    }

    public void showLoginScreen() {
        loadScene(LOGIN_VIEW, "Sign In");
    }

    public void showRegisterScreen() {
        loadScene(REGISTER_VIEW, "Create Account");
    }

    public void showMainScreen(String userName) {
        loadScene(MAIN_VIEW, userName != null ? "Chat - " + userName : "Chat Application",
                "/styles/main.css");
    }

    private void loadScene(String fxmlPath, String title, String cssPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML not found: " + fxmlPath);
            }

            Parent newRoot = FXMLLoader.load(fxmlUrl);

            if (scene == null) {
                scene = new Scene(newRoot, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                primaryStage.setScene(scene);
            } else {
                applyFadeTransition(scene.getRoot(), newRoot);
                scene.setRoot(newRoot);
            }

            if (cssPath != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }

            primaryStage.setTitle(title);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + fxmlPath, e);
        }
    }

    private void loadScene(String fxmlPath, String title) {
        loadScene(fxmlPath, title, null);
    }

    private void applyFadeTransition(Parent oldRoot, Parent newRoot) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldRoot);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newRoot);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeOut.setOnFinished(e -> fadeIn.play());
        fadeOut.play();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
