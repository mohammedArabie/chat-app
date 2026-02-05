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

import static javafx.scene.Cursor.HAND;

public class ChatController {

    @FXML
    private Label chatNameLabel;
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

        chatNameLabel.textProperty().bind(viewModel.chatTitleProperty());

        messageListView.setItems(viewModel.getMessageHistory());
        messageListView.setCellFactory(param -> new MessageCell());

        viewModel.getMessageHistory().addListener((ListChangeListener<MessageDTO>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    messageListView.scrollTo(viewModel.getMessageHistory().size() - 1);
                }
            }
        });

        messageInput.disableProperty().bind(viewModel.selectedChatProperty().isNull());
        sendButton.disableProperty().bind(viewModel.selectedChatProperty().isNull());
        attachButton.disableProperty().bind(viewModel.selectedChatProperty().isNull());

        chatNameLabel.setOnMouseClicked(event -> {
            if (viewModel.selectedChatProperty().get() != null) {
                viewModel.toggleInfoPane();
            }
        });

        chatNameLabel.setCursor(HAND);
    }

    @FXML
    private void onSendMessage() {
        String text = messageInput.getText();
        if (text != null && !text.trim().isEmpty()) {
            viewModel.sendMessage(text);
            messageInput.clear();
        }
    }

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