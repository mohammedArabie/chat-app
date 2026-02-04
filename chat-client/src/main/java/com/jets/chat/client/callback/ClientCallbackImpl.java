package com.jets.chat.client.callback;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.util.SceneManager;
import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;
import javafx.application.Platform;
import com.jets.chat.common.dto.AnnouncementDTO;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog; // Add this import
import javafx.scene.control.Label; // Add this import

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {

    private final ChatViewModel chatViewModel;

    public ClientCallbackImpl(ChatViewModel chatViewModel) throws RemoteException {
        super();
        this.chatViewModel = chatViewModel;
    }

    @Override
    public void receiveMessage(MessageDTO message) throws RemoteException {
        // Logic: Add the message to the active history list
        // Platform.runLater is MANDATORY because RMI calls are on a background thread
        Platform.runLater(() -> {
            // Check if the received message belongs to the chat currently open
            if (chatViewModel.selectedChatProperty().get() != null) {
                // You might want to check message.chatId() here
                chatViewModel.getMessageHistory().add(message);
            }

            // Optional: Update the last message snippet in the sidebar list
            // viewModel.updateSidebarSnippet(message);
        });
    }

    @Override
    public void updateContactStatus(Integer contactId, UserStatus status) throws RemoteException {
        Platform.runLater(() -> {
            // Logic to update the green/gray dot in the sidebar
            System.out.println("Contact " + contactId + " is now " + status);
        });
    }

    @Override
    public void receiveAnnouncement(String title, String content) throws RemoteException {
        Platform.runLater(() -> {
            // Logic to show a popup or notification for system announcements
            System.out.println("ANNOUNCEMENT: " + title + " - " + content);
        });
    }

    @Override
    public void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException {
        Platform.runLater(() -> {
            // Create a clean, simple dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Announcement");

            // Create styled label
            Label label = new Label(announcement.getContent());
            label.setWrapText(true);

            // Build minimal style - only apply what's specified
            StringBuilder style = new StringBuilder();

            // Font family if specified
            if (announcement.getFontStyle() != null) {
                style.append("-fx-font-family: '").append(announcement.getFontStyle())
                        .append("'; ");
            }

            // Bold if specified (most important)
            if (announcement.isBold()) {
                style.append("-fx-font-weight: bold; ");
            }

            // Color if specified (second most noticeable)
            if (announcement.getFontColor() != null) {
                style.append("-fx-text-fill: ").append(announcement.getFontColor()).append("; ");
            }

            // Italic if specified
            if (announcement.isItalic()) {
                style.append("-fx-font-style: italic; ");
            }

            style.append("-fx-font-size: 16px; ");
            label.setStyle(style.toString());

            // Simple layout
            javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(label);
            box.setStyle("-fx-padding: 20;");

            dialog.getDialogPane().setContent(box);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            dialog.getDialogPane().setPrefSize(300, 300);

            // Set owner window
            javafx.stage.Stage primaryStage = SceneManager.getInstance().getPrimaryStage();
            if (primaryStage != null) {
                dialog.initOwner(primaryStage);
            }

            dialog.show();
        });
    }
}
