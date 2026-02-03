package com.jets.chat.client.ui.stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StageCoordinator {

    private static final StageCoordinator INSTANCE = new StageCoordinator();
    private Stage primaryStage;
    private final Map<StageName, Scene> sceneCache = new HashMap<>();

    private StageCoordinator() {
    }

    public static StageCoordinator getInstance() {
        return INSTANCE;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    public void switchToMainChat() {
        loadScene(StageName.MAIN_LAYOUT);
    }

    public void switchToLogin() {
        loadScene(StageName.LOGIN);
    }

    private void loadScene(StageName stageName) {
        try {
            // Check cache first if you want to keep state,
            // or always reload for fresh UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource(stageName.getFxmlPath()));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Apply global CSS if needed
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("JETS Chat - " + stageName.name());
            primaryStage.centerOnScreen();

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + stageName.getFxmlPath());
            e.printStackTrace();
        }
    }
}