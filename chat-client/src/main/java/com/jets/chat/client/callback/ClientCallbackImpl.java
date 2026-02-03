package com.jets.chat.client.callback;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;
import javafx.application.Platform;
import com.jets.chat.common.dto.AnnouncementDTO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

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
    private final Stage primaryStage;

    public ClientCallbackImpl(Stage primaryStage) throws RemoteException {
        this.primaryStage = primaryStage;
    }
    @Override
    public void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Server Announcement");
            alert.setHeaderText(null);
            alert.setContentText(announcement.getContent());
            alert.initOwner(primaryStage);
            alert.show();
        });
    }
}
