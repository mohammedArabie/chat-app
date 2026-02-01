package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

import static com.jets.chat.common.util.Functions.getInitials;

public class MainController implements Initializable {

    private final ChatViewModel chatViewModel = new ChatViewModel();
    private final Long currentUserId = 1L;
    @FXML
    private SideBarController sideBarController;
    @FXML
    private ChatController chatController;
    @FXML
    private VBox infoPaneRoot;
    @FXML
    private Label infoName;
    @FXML
    private Label infoInitials;
    @FXML
    private Label infoEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sideBarController != null) {
            sideBarController.init(chatViewModel);
        }
        if (chatController != null) {
            chatController.init(chatViewModel);
        }

        setupRightPaneBindings();

        // TODO: fetch it from the session
        chatViewModel.loadUserChats(currentUserId);
    }

    private void setupRightPaneBindings() {
        // Bind visibility to the ViewModel property
        infoPaneRoot.visibleProperty().bind(chatViewModel.showInfoPaneProperty());
        infoPaneRoot.managedProperty().bind(chatViewModel.showInfoPaneProperty());

        // Bind data
        infoName.textProperty().bind(chatViewModel.chatTitleProperty());
        infoEmail.textProperty().bind(chatViewModel.contactEmailProperty());

        chatViewModel.currentContactProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                infoInitials.setText(getInitials(newVal));
            }
        });
    }
}