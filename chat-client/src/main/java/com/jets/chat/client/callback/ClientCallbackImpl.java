package com.jets.chat.client.callback;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.util.SystemNotificationUtil;
import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;
import javafx.application.Platform;

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
        Platform.runLater(() -> {
            if (chatViewModel.selectedChatProperty().get() != null) {
                chatViewModel.getMessageHistory().add(message);
            }

            if (chatViewModel.isSystemNotificationsEnabled()) {
                SystemNotificationUtil.showInfoNotification("You have recieved a new message",
                        message.content());
            }
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
    public void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException {
        SystemNotificationUtil.showAnnouncement(announcement);
    }
}