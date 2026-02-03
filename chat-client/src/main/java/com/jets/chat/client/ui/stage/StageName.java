package com.jets.chat.client.ui.stage;

public enum StageName {
    LOGIN("/views/Login.fxml"), REGISTER("/views/Register.fxml"), MAIN_LAYOUT(
            "/views/MainLayout.fxml");

    private final String fxmlPath;

    StageName(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}