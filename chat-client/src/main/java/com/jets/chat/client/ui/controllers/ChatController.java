package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.components.MessageCell;
import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.common.dto.MessageDTO;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class ChatController {

    @FXML
    private Label chatNameLabel;
    @FXML
    private Label statusDotLabel;
    @FXML
    private Label statusTextLabel;
    @FXML
    private ListView<MessageDTO> messageListView;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private Button attachButton;

    private ChatViewModel viewModel;

    /**
     * Called by MainController to inject the shared ViewModel
     */
    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        // 1. Bind Header Name
        chatNameLabel.textProperty().bind(viewModel.chatTitleProperty());

        // 2. Setup Message ListView
        messageListView.setItems(viewModel.getMessageHistory());
        messageListView.setCellFactory(param -> new MessageCell());

        // 3. Auto-scroll to bottom when new messages are added
        viewModel.getMessageHistory().addListener((ListChangeListener<MessageDTO>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    messageListView.scrollTo(viewModel.getMessageHistory().size() - 1);
                }
            }
        });

        // 4. Update Header Status based on selected contact
        viewModel.currentContactProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String status = "ONLINE";
                statusTextLabel.setText(
                        status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase());

                if ("ONLINE".equals(status)) {
                    statusDotLabel.setStyle("-fx-text-fill: #3BA55D;");
                } else if ("AWAY".equals(status)) {
                    statusDotLabel.setStyle("-fx-text-fill: #FAA61A;");
                } else {
                    statusDotLabel.setStyle("-fx-text-fill: #747F8D;");
                }
            }
        });

        // 5. Disable input if no chat is selected
        messageInput.disableProperty().bind(viewModel.selectedChatProperty().isNull());
        sendButton.disableProperty().bind(viewModel.selectedChatProperty().isNull());
        attachButton.disableProperty().bind(viewModel.selectedChatProperty().isNull());

        // Make the chat name clickable to open/hide the Info Pane
        chatNameLabel.setOnMouseClicked(event -> {
            if (viewModel.selectedChatProperty().get() != null) {
                viewModel.toggleInfoPane();
            }
        });

        // Optional: change cursor to hand when hovering over name
        chatNameLabel.setCursor(javafx.scene.Cursor.HAND);
    }

    /**
     * Triggered when the user clicks 'Send' or presses Enter in the TextField
     */
    @FXML
    private void onSendMessage() {
        String text = messageInput.getText();
        if (text != null && !text.trim().isEmpty()) {
            viewModel.sendMessage(text);
            messageInput.clear();
        }
    }

    /**
     * Triggered when the user clicks the attachment button
     */
    @FXML
    private void onAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
                new FileChooser.ExtensionFilter("Archives", "*.zip", "*.rar", "*.7z"));

        File selectedFile = fileChooser.showOpenDialog(attachButton.getScene().getWindow());

        if (selectedFile != null) {
            viewModel.sendFileMessage(selectedFile);
        }
    }
}