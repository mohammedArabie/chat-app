package com.jets.chat.client;

import com.jets.chat.client.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            SceneManager.getInstance().initialize(stage);
            SceneManager.getInstance().showLoginScreen();
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