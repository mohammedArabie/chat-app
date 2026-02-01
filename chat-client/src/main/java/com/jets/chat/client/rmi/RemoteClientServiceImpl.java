package com.jets.chat.client.rmi;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.rmi.RemoteClientService;
import javafx.application.Platform;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteClientServiceImpl extends UnicastRemoteObject implements RemoteClientService {

    private final ChatViewModel chatViewModel;

    public RemoteClientServiceImpl(ChatViewModel chatViewModel) throws RemoteException {
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
}