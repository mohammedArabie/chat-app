package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class SettingsPaneController {

    @FXML
    private CheckBox systemNotificationCheckBox;

    private ChatViewModel viewModel;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind the checkbox to the view model property
        systemNotificationCheckBox.selectedProperty()
                .bindBidirectional(viewModel.enableSystemNotificationsProperty());
    }

    @FXML
    private void onLogoutClicked() {
        viewModel.logout();
    }
}