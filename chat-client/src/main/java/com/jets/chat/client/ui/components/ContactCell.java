package com.jets.chat.client.ui.components;

import com.jets.chat.common.dto.ChatSummaryDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;

import static com.jets.chat.common.util.Functions.getInitials;

public class ContactCell extends ListCell<ChatSummaryDTO> {

    @FXML
    private HBox root;
    @FXML
    private Label nameLabel;
    @FXML
    private Label lastMessageLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label initialsLabel;
    @FXML
    private Circle statusDot;

    private FXMLLoader loader;

    @Override
    protected void updateItem(ChatSummaryDTO chat, boolean empty) {
        super.updateItem(chat, empty);

        if (empty || chat == null) {
            setGraphic(null);
            setText(null);
        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("/fxml/ContactItem.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Data extraction from ChatSummaryDTO
            var contact = chat.chatName();

            nameLabel.setText(contact);
            lastMessageLabel
                    .setText(chat.lastMessage() != null ? chat.lastMessage() : "No messages yet");
            timeLabel.setText(chat.getFormattedTime());

            // Set initials (SC, MJ, etc.)
            initialsLabel.setText(getInitials(contact));

            // TODO: to be removed?
            String status = "ONLINE";
            if ("ONLINE".equals(status)) {
                statusDot.setFill(Color.web("#3BA55D"));
            } else if ("AWAY".equals(status)) {
                statusDot.setFill(Color.web("#FAA61A"));
            } else {
                statusDot.setFill(Color.web("#747F8D"));
            }

            root.getStyleClass().add("contact-item-container");

            setGraphic(root);
        }
    }
}