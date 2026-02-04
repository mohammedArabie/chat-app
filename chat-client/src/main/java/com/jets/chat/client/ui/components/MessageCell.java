package com.jets.chat.client.ui.components;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.util.ClientManager;
import com.jets.chat.common.dto.MessageDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class MessageCell extends ListCell<MessageDTO> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
    private ChatViewModel viewModel;

    public MessageCell() {
    }

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

            messageText.getStyleClass().removeAll("bubble-sent", "bubble-received");
            bubbleWrapper.getChildren().clear();

            if (message.isFileMessage() && message.fileMetadata() != null) {
                renderFileMessage(message);
            } else {
                renderTextMessage(message);
            }

            timeLabel.setText(message.time().format(TIME_FORMATTER));

            if (message.isSentByMe()) {
                rootPane.setAlignment(Pos.CENTER_RIGHT);
                vBoxContainer.setAlignment(Pos.TOP_RIGHT);
                bubbleWrapper.setAlignment(Pos.TOP_RIGHT);
                avatarContainer.setVisible(false);
                avatarContainer.setManaged(false);
                if (rootPane.getChildren().contains(avatarContainer)) {
                    rootPane.getChildren().remove(avatarContainer);
                }
            } else {
                rootPane.setAlignment(Pos.CENTER_LEFT);
                vBoxContainer.setAlignment(Pos.TOP_LEFT);
                bubbleWrapper.setAlignment(Pos.TOP_LEFT);
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

    private void renderTextMessage(MessageDTO message) {
        messageText.setText(message.content());

        if (message.isSentByMe()) {
            messageText.getStyleClass().add("bubble-sent");
        } else {
            messageText.getStyleClass().add("bubble-received");
        }

        bubbleWrapper.getChildren().add(messageText);
    }

    private void renderFileMessage(MessageDTO message) {
        VBox fileContainer = new VBox(5);
        fileContainer.getStyleClass()
                .add(message.isSentByMe() ? "file-bubble-sent" : "file-bubble-received");

        HBox fileInfo = new HBox(8);
        fileInfo.setAlignment(Pos.CENTER_LEFT);

        Label fileIcon = new Label(getFileIcon(message.fileMetadata().contentType()));
        fileIcon.setStyle("-fx-font-size: 24px;");

        VBox fileDetails = new VBox(2);
        Label fileName = new Label(message.fileMetadata().fileName());
        fileName.getStyleClass().add("file-name-label");
        fileName.setWrapText(true);
        fileName.setMaxWidth(250);

        Label fileSize = new Label(formatFileSize(message.fileMetadata().fileSize()));
        fileSize.getStyleClass().add("file-size-label");

        fileDetails.getChildren().addAll(fileName, fileSize);
        fileInfo.getChildren().addAll(fileIcon, fileDetails);

        boolean fileExistsLocally = checkIfFileExistsLocally(message);
        Button actionButton = new Button(fileExistsLocally ? "📂 Open" : "📥 Download");
        actionButton.getStyleClass().add("file-action-button");
        actionButton.setOnAction(e -> {
            ClientManager.getInstance().getChatViewModel()
                    .downloadAndOpenFile(message.fileMetadata());
            actionButton.setText("📂 Open");
        });

        fileContainer.getChildren().addAll(fileInfo, actionButton);
        bubbleWrapper.getChildren().add(fileContainer);
    }

    private boolean checkIfFileExistsLocally(MessageDTO message) {
        try {
            if (message.isSentByMe() && message.fileMetadata().filePath() != null) {
                Path originalPath = Paths.get(message.fileMetadata().filePath());
                if (Files.exists(originalPath)) {
                    return true;
                }
            }

            String userHome = System.getProperty("user.home");
            long userId = com.jets.chat.client.util.SessionManager.getUserId();
            Path downloadPath = Paths.get(userHome, "chat-app", String.valueOf(userId), "files",
                    message.fileMetadata().fileName());
            return Files.exists(downloadPath);
        } catch (Exception e) {
            return false;
        }
    }

    private String getFileIcon(String contentType) {
        if (contentType == null)
            return "📄";

        if (contentType.startsWith("image/"))
            return "🖼️";
        if (contentType.startsWith("video/"))
            return "🎥";
        if (contentType.startsWith("audio/"))
            return "🎵";
        if (contentType.contains("pdf"))
            return "📕";
        if (contentType.contains("word") || contentType.contains("document"))
            return "📘";
        if (contentType.contains("excel") || contentType.contains("spreadsheet"))
            return "📊";
        if (contentType.contains("powerpoint") || contentType.contains("presentation"))
            return "📊";
        if (contentType.contains("zip") || contentType.contains("rar")
                || contentType.contains("archive"))
            return "📦";
        if (contentType.contains("text"))
            return "📝";

        return "📄";
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}