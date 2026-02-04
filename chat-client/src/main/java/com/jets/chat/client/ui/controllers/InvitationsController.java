package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.ui.viewmodel.RightPaneView;
import com.jets.chat.client.util.SessionManager;
import com.jets.chat.common.dto.InvitationDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class InvitationsController {
    @FXML
    private Label pendingCountLabel;
    @FXML
    private VBox invitationListContainer;
    @FXML
    private VBox emptyStateBox;
    @FXML
    private ScrollPane invitationScrollPane;

    private ChatViewModel viewModel;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        pendingCountLabel.textProperty()
                .bind(Bindings.size(viewModel.getPendingInvitations()).asString("%d pending"));

        BooleanBinding isListEmpty = Bindings.isEmpty(viewModel.getPendingInvitations());

        emptyStateBox.visibleProperty().bind(isListEmpty);
        emptyStateBox.managedProperty().bind(isListEmpty);

        invitationScrollPane.visibleProperty().bind(isListEmpty.not());
        invitationScrollPane.managedProperty().bind(isListEmpty.not());

        viewModel.getPendingInvitations().addListener((ListChangeListener<InvitationDTO>) c -> {
            refreshInvitationCards();
        });

        refreshInvitationCards();
    }

    private void refreshInvitationCards() {
        invitationListContainer.getChildren().clear();
        System.out.println("Invitations " + viewModel.getPendingInvitations() + " "
                + SessionManager.getUserId());
        for (InvitationDTO dto : viewModel.getPendingInvitations()) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/InvitationCard.fxml"));
                Parent card = loader.load();

                InvitationCardController controller = loader.getController();
                controller.setData(dto, viewModel);

                invitationListContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onClose() {
        viewModel.activeRightViewProperty().set(RightPaneView.NONE);
    }
}