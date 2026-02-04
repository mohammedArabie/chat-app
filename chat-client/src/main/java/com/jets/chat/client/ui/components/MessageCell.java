package com.jets.chat.client.ui.components;

import com.jets.chat.common.dto.MessageDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MessageCell extends ListCell<MessageDTO> {

    @FXML
    private HBox rootPane;
    @FXML
    private VBox vBoxContainer;
    @FXML
    private HBox bubbleWrapper;
    @FXML
    private StackPane avatarContainer;
    @FXML
    private Label messageText;
    @FXML
    private Label timeLabel;
    @FXML
    private Label initialsLabel;

    private FXMLLoader loader;

    @Override
    protected void updateItem(MessageDTO message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setGraphic(null);
            setText(null);
        } else {
            if (loader == null) {
                loader = new FXMLLoader(getClass().getResource("/fxml/MessageBubble.fxml"));
                loader.setController(this);
                try {
                    loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 1. Set text content and time
            messageText.setText(message.content());
            timeLabel.setText(message.time().toLocalTime().toString());

            // 2. Clear previous specific styles to avoid "leakage" during cell recycling
            messageText.getStyleClass().removeAll("bubble-sent", "bubble-received");

            // 3. APPLY CONDITIONAL LOGIC
            if (message.isSentByMe()) {
                // ALIGN TO THE RIGHT (Your messages)
                rootPane.setAlignment(Pos.CENTER_RIGHT);
                vBoxContainer.setAlignment(Pos.TOP_RIGHT);
                bubbleWrapper.setAlignment(Pos.TOP_RIGHT);

                messageText.getStyleClass().add("bubble-sent");

                // Hide avatar for your own messages
                avatarContainer.setVisible(false);
                avatarContainer.setManaged(false);

                // Ensure the bubble is the last element in the HBox (rightmost)
                if (rootPane.getChildren().contains(avatarContainer)) {
                    rootPane.getChildren().remove(avatarContainer);
                }
            } else {
                // ALIGN TO THE LEFT (Received messages)
                rootPane.setAlignment(Pos.CENTER_LEFT);
                vBoxContainer.setAlignment(Pos.TOP_LEFT);
                bubbleWrapper.setAlignment(Pos.TOP_LEFT);

                messageText.getStyleClass().add("bubble-received");

                avatarContainer.setVisible(true);
                avatarContainer.setManaged(true);

                // Set initials from sender name
                if (message.senderName() != null && !message.senderName().isEmpty()) {
                    String initials = getInitials(message.senderName());
                    initialsLabel.setText(initials);
                } else {
                    initialsLabel.setText("?");
                }

                if (!rootPane.getChildren().contains(avatarContainer)) {
                    rootPane.getChildren().add(0, avatarContainer);
                }
            }

            setGraphic(rootPane);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                    .toUpperCase();
        } else if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return "?";
    }
}