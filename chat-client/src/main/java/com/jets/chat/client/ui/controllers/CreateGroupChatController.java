package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.ui.viewmodel.RightPaneView;
import com.jets.chat.client.util.ClientManager;
import com.jets.chat.client.util.SessionManager;
import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.enums.ChatType;
import com.jets.chat.common.rmi.RemoteChatService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateGroupChatController {
    private final ObservableList<ChatSummaryDTO> contacts = FXCollections.observableArrayList();
    private final ObservableList<Long> selectedMemberIds = FXCollections.observableArrayList();
    @FXML
    private TextField groupNameField;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<ChatSummaryDTO> contactsListView;
    @FXML
    private Label selectionLabel;
    @FXML
    private VBox successBox;
    private ChatViewModel viewModel;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;
        loadContacts();
    }

    private void loadContacts() {
        new Thread(() -> {
            try {
                RemoteChatService chatService = ClientManager.getInstance().getRemoteChatService();
                List<ChatSummaryDTO> chats = chatService.getUserChats(SessionManager.getUserId());

                List<ChatSummaryDTO> individualContacts = chats.stream()
                        .filter(chat -> chat.chatType().equals(ChatType.PRIVATE))
                        .filter(chat -> chat.chatName() != null && !chat.chatName().isEmpty())
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    contacts.setAll(individualContacts);
                    contactsListView.setItems(contacts);

                    contactsListView.setCellFactory(param -> new ListCell<ChatSummaryDTO>() {
                        private final CheckBox checkBox = new CheckBox();
                        private final Label nameLabel = new Label();
                        private final HBox hbox = new HBox(10, checkBox, nameLabel);

                        {
                            checkBox.setOnAction(e -> {
                                ChatSummaryDTO chat = getItem();
                                if (chat != null) {
                                    if (checkBox.isSelected()) {
                                        if (!selectedMemberIds.contains(chat.chatId())) {
                                            selectedMemberIds.add(chat.chatId());
                                        }
                                    } else {
                                        selectedMemberIds.remove(chat.chatId());
                                    }
                                    updateSelectionLabel();
                                }
                            });
                        }

                        @Override
                        protected void updateItem(ChatSummaryDTO chat, boolean empty) {
                            super.updateItem(chat, empty);
                            if (empty || chat == null) {
                                setGraphic(null);
                            } else {
                                nameLabel.setText(chat.chatName());
                                checkBox.setSelected(selectedMemberIds.contains(chat.chatId()));
                                setGraphic(hbox);
                            }
                        }
                    });
                });
            } catch (RemoteException e) {
                System.err.println("Error loading contacts: " + e.getMessage());
            }
        }).start();
    }

    private void updateSelectionLabel() {
        selectionLabel.setText("Selected: " + selectedMemberIds.size() + " members");
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().toLowerCase();
        if (query == null || query.isEmpty()) {
            contactsListView.setItems(contacts);
        } else {
            ObservableList<ChatSummaryDTO> filtered = contacts
                    .filtered(chat -> chat.chatName() != null
                            && chat.chatName().toLowerCase().contains(query));
            contactsListView.setItems(filtered);
        }
    }

    @FXML
    private void onCreateGroup() {
        String groupName = groupNameField.getText();
        if (groupName == null || groupName.trim().isEmpty()) {
            showError("Please enter a group name");
            return;
        }

        if (selectedMemberIds.isEmpty()) {
            showError("Please select at least one member");
            return;
        }

        new Thread(() -> {
            try {
                RemoteChatService chatService = ClientManager.getInstance().getRemoteChatService();
                long ownerId = SessionManager.getUserId();
                List<Long> memberIds = new ArrayList<>(selectedMemberIds);
                System.out.println(memberIds);
                long chatId = chatService.createGroup(groupName, ownerId, memberIds);

                Platform.runLater(() -> {
                    successBox.setVisible(true);
                    successBox.setManaged(true);

                    viewModel.updateChatList();

                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            Platform.runLater(() -> {
                                viewModel.activeRightViewProperty().set(RightPaneView.NONE);
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            } catch (RemoteException e) {
                Platform.runLater(() -> showError("Failed to create group: " + e.getMessage()));
            }
        }).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onClose() {
        viewModel.activeRightViewProperty().set(RightPaneView.NONE);
    }
}
