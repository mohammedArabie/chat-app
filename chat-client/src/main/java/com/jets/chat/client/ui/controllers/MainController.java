package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.ui.viewmodel.RightPaneView;
import com.jets.chat.client.util.ClientManager;
import com.jets.chat.client.util.SessionManager;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.jets.chat.common.util.Functions.getInitials;

public class MainController implements Initializable {

    private final ChatViewModel chatViewModel = ClientManager.getInstance().getChatViewModel();
    @FXML
    private SideBarController sideBarController;
    @FXML
    private ChatController chatController;
    @FXML
    private VBox infoPaneRoot;
    @FXML
    private VBox settingsPane;
    @FXML
    private Label infoName;
    @FXML
    private Label infoInitials;
    @FXML
    private Label infoEmail;
    @FXML
    private SettingsPaneController settingsPaneController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sideBarController != null) {
            sideBarController.init(chatViewModel);
        }
        if (chatController != null) {
            chatController.init(chatViewModel);
        }
        if (settingsPaneController != null) {
            settingsPaneController.init(chatViewModel);
        }

        setupRightPaneBindings();
        chatViewModel.loadUserChats(SessionManager.getUserId());
        chatViewModel.fetchInvitationsFromServer();
        // chatViewModel.startPolling();
    }

    private void setupRightPaneBindings() {
        infoPaneRoot.visibleProperty()
                .bind(Bindings.createBooleanBinding(
                        () -> chatViewModel.activeRightViewProperty().get() != RightPaneView.NONE,
                        chatViewModel.activeRightViewProperty()));
        infoPaneRoot.managedProperty().bind(infoPaneRoot.visibleProperty());

        // Bind visibility to the ViewModel property for settings pane
        settingsPane.visibleProperty().bind(chatViewModel.showSettingsPaneProperty());
        settingsPane.managedProperty().bind(chatViewModel.showSettingsPaneProperty());

        // Bind data
        infoName.textProperty().bind(chatViewModel.chatTitleProperty());
        infoEmail.textProperty().bind(chatViewModel.contactEmailProperty());

        chatViewModel.currentContactProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                infoInitials.setText(getInitials(newVal));
            }
        });

        chatViewModel.activeRightViewProperty().addListener((obs, oldView, newView) -> {
            if (newView != RightPaneView.NONE) {
                loadRightSubView(newView);
            }
        });
    }

    private void loadRightSubView(RightPaneView view) {
        String fxml = switch (view) {
            case CONTACT_INFO, PROFILE -> "/fxml/ContactInfoView.fxml";
            case INVITATIONS -> "/fxml/InvitationsView.fxml";
            case ADD_CONTACT -> "/fxml/AddContactView.fxml";
            default -> null;
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent node = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ContactInfoController) {
                ((ContactInfoController) controller).init(chatViewModel);
            } else if (controller instanceof InvitationsController) {
                ((InvitationsController) controller).init(chatViewModel);
            } else if (controller instanceof AddContactController) {
                ((AddContactController) controller).init(chatViewModel);
            }

            infoPaneRoot.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}