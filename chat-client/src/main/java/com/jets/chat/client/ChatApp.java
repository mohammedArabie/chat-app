package com.jets.chat.client;

import com.jets.chat.client.ui.stage.StageCoordinator;
import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StageCoordinator coordinator = StageCoordinator.getInstance();
        coordinator.init(primaryStage);

        coordinator.switchToMainChat();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
