package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.ui.viewmodel.RightPaneView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

import static com.jets.chat.common.util.Functions.getInitials;

public class ContactInfoController {
    @FXML
    private Label nameLabel, handleLabel, emailLabel, initialsLabel;
    @FXML
    private Circle profileCircle;

    private ChatViewModel viewModel;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        nameLabel.textProperty().bind(viewModel.chatTitleProperty());
        emailLabel.textProperty().bind(viewModel.contactEmailProperty());

        viewModel.currentContactProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleLabel.setText("@" + newVal.toLowerCase().replace(" ", ""));
                initialsLabel.setText(getInitials(newVal));
            }
        });
    }

    @FXML
    private void onClose() {
        viewModel.activeRightViewProperty().set(RightPaneView.NONE);
    }
}