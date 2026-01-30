package com.jets.chat.client.controller;

import com.jets.chat.common.enums.UserStatus;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class ProfileStatusController {
    @FXML
    private ToggleButton onlineBtn, awayBtn, dndBtn, offlineBtn;
    private ToggleGroup statusGroup;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        statusGroup = new ToggleGroup();
        onlineBtn.setToggleGroup(statusGroup);
        awayBtn.setToggleGroup(statusGroup);
        dndBtn.setToggleGroup(statusGroup);
        offlineBtn.setToggleGroup(statusGroup);

        onlineBtn.setUserData(UserStatus.AVAILABLE);
        awayBtn.setUserData(UserStatus.AWAY);
        dndBtn.setUserData(UserStatus.BUSY);
        offlineBtn.setUserData(UserStatus.OFFLINE);

        statusGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                UserStatus s = (UserStatus) newT.getUserData();
                userService.changeStatus(1, s);
                updateVisualStyles();
            }
        });
    }

    public void setStatus(UserStatus status) {
        if (status == null)
            return;
        statusGroup.getToggles().forEach(toggle -> {
            if (toggle.getUserData().equals(status)) {
                statusGroup.selectToggle(toggle);
            }
        });
        updateVisualStyles();
    }

    private void updateVisualStyles() {
        onlineBtn.getStyleClass().removeAll("status-online", "status-away", "status-dnd",
                "status-offline");
        awayBtn.getStyleClass().removeAll("status-online", "status-away", "status-dnd",
                "status-offline");
        dndBtn.getStyleClass().removeAll("status-online", "status-away", "status-dnd",
                "status-offline");
        offlineBtn.getStyleClass().removeAll("status-online", "status-away", "status-dnd",
                "status-offline");

        ToggleButton selected = (ToggleButton) statusGroup.getSelectedToggle();
        if (selected != null) {
            UserStatus s = (UserStatus) selected.getUserData();
            selected.getStyleClass().add(mapStatusToStyleClass(s));
        }
    }

    private String mapStatusToStyleClass(UserStatus status) {
        return switch (status) {
            case AVAILABLE -> "status-online";
            case AWAY -> "status-away";
            case BUSY -> "status-dnd";
            case OFFLINE -> "status-offline";
        };
    }
}