package com.jets.chat.client.callback;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.util.ClientManager;
import com.jets.chat.client.util.SystemNotificationUtil;
import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.InvitationDTO;
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
            chatViewModel.addMessage(message);
            if (chatViewModel.isSystemNotificationsEnabled()) {
                String snippet = message.isFileMessage()
                        ? "📎 File: " + message.content()
                        : message.content();
                SystemNotificationUtil
                        .showInfoNotification("New message from " + message.senderName(), snippet);
            }
        });
    }

    @Override
    public void reloadChats() throws RemoteException {
        chatViewModel.updateChatList();
    }

    @Override
    public void updateContactStatus(Long contactId, UserStatus status) throws RemoteException {
        Platform.runLater(() -> {
            System.out.println("Contact " + contactId + " is now " + status);
            var chatList = chatViewModel.getChatSummaryList();
            for (ChatSummaryDTO chat : chatList) {
                try {
                    if (ClientManager.getInstance().getRemoteChatService().isUserInChat(contactId,
                            chat.chatId())) {
                        ChatSummaryDTO updatedChat = new ChatSummaryDTO(chat.chatId(),
                                chat.chatName(), chat.lastMessage(), chat.lastMessageTime(),
                                chat.lastMessageSender(), status);
                        int index = chatList.indexOf(chat);
                        if (index >= 0) {
                            chatList.set(index, updatedChat);
                        }
                        break;
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException {
        SystemNotificationUtil.showAnnouncement(announcement);
    }

    @Override
    public void onInvitationReceived(InvitationDTO invitation) throws RemoteException {
        Platform.runLater(() -> {
            chatViewModel.getPendingInvitations().add(invitation);
            if (chatViewModel.isSystemNotificationsEnabled()) {
                SystemNotificationUtil.showInfoNotification("New Contact Request",
                        "You have a new contact request from " + invitation.userName());
            }
        });
    }

    @Override
    public void onInvitationAccepted(String contactName) throws RemoteException {
        Platform.runLater(() -> {
            if (chatViewModel.isSystemNotificationsEnabled()) {
                SystemNotificationUtil.showInfoNotification("Contact Request Accepted",
                        contactName + " has accepted your contact request");
            }
            chatViewModel.updateChatList();
        });
    }
}