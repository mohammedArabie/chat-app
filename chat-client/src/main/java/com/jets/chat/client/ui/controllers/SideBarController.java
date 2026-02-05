package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.components.ContactCell;
import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.common.dto.ChatSummaryDTO;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

public class SideBarController {

    @FXML
    private ListView<ChatSummaryDTO> contactListView;
    @FXML
    private TextField searchField;

    private ChatViewModel viewModel;

    @FXML
    private Label requestCount;
    @FXML
    private Circle requestBadge;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        FilteredList<ChatSummaryDTO> filteredData = new FilteredList<>(
                viewModel.getChatSummaryList(), p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(chat -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return chat.chatName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        requestCount.textProperty().bind(viewModel.pendingRequestsCountProperty().asString());
        requestBadge.visibleProperty()
                .bind(viewModel.pendingRequestsCountProperty().greaterThan(0));
        requestCount.visibleProperty()
                .bind(viewModel.pendingRequestsCountProperty().greaterThan(0));
        requestCount.visibleProperty()
                .bind(viewModel.pendingRequestsCountProperty().greaterThan(0));

        contactListView.setItems(filteredData);
        contactListView.setCellFactory(param -> new ContactCell());

        contactListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        viewModel.selectedChatProperty().set(newVal);
                    }
                });

        viewModel.selectedChatProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                contactListView.getSelectionModel().clearSelection();
            } else if (contactListView.getSelectionModel().getSelectedItem() != newVal) {
                contactListView.getSelectionModel().select(newVal);
            }
        });
    }

    @FXML
    private void onSettingsClicked() {
        viewModel.showSettings();
    }

    @FXML
    private void onAddContactClicked() {
        viewModel.showAddContact();
    }

    @FXML
    private void onCreateGroupClicked() {
        viewModel.showCreateGroup();
    }

    @FXML
    private void onRequestsClicked(MouseEvent event) {
        viewModel.showInvitations();
    }
}