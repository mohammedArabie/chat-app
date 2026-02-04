package com.jets.chat.client.callback;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.util.SceneManager;
import com.jets.chat.client.util.SessionManager;
import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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
    public void reloadChats() throws RemoteException {
        this.chatViewModel.loadUserChats(SessionManager.getUserId());
    }

    @Override
    public void updateContactStatus(Long contactId, UserStatus status) throws RemoteException {
        Platform.runLater(() -> {
            // Update the status in the chat summary list
            System.out.println("Contact " + contactId + " is now " + status);

            // Find and update the contact in the chat list
            var chatList = chatViewModel.getChatSummaryList();
            for (ChatSummaryDTO chat : chatList) {
                if (chat.chatId() == contactId) {
                    // Create a new ChatSummaryDTO with updated status
                    ChatSummaryDTO updatedChat = new ChatSummaryDTO(chat.chatId(), chat.chatName(),
                            chat.lastMessage(), chat.lastMessageTime(), chat.lastMessageSender(),
                            status);

                    int index = chatList.indexOf(chat);
                    if (index >= 0) {
                        chatList.set(index, updatedChat);
                    }
                    break;
                }
            }
        });
    }

    @Override
    public void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException {
        SystemNotificationUtil.showAnnouncement(announcement);
    }
}