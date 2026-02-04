package com.jets.chat.client.ui.viewmodel;

import com.jets.chat.client.util.ClientManager;
import com.jets.chat.client.util.SceneManager;
import com.jets.chat.client.util.SessionManager;
import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.InvitationDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.rmi.RemoteChatService;
import com.jets.chat.common.rmi.RemoteContactsService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatViewModel {

    private final ObservableList<ChatSummaryDTO> chatSummaryList = FXCollections
            .observableArrayList();
    private final ObjectProperty<ChatSummaryDTO> selectedChat = new SimpleObjectProperty<>();

    private final ObservableList<MessageDTO> messageHistory = FXCollections.observableArrayList();
    private final ObjectProperty<String> currentContact = new SimpleObjectProperty<>();

    private final StringProperty chatTitle = new SimpleStringProperty("Select a conversation");
    private final StringProperty contactEmail = new SimpleStringProperty("");
    private final BooleanProperty showInfoPane = new SimpleBooleanProperty(false);
    private final ObjectProperty<RightPaneView> activeRightView = new SimpleObjectProperty<>(
            RightPaneView.NONE);

    private final ObservableList<InvitationDTO> pendingInvitations = FXCollections
            .observableArrayList();
    private final IntegerProperty pendingRequestsCount = new SimpleIntegerProperty(0);

    private final IntegerProperty unreadAnnouncementsCount = new SimpleIntegerProperty(0);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final BooleanProperty showSettingsPane = new SimpleBooleanProperty(false);
    private final BooleanProperty enableSystemNotifications = new SimpleBooleanProperty(true);

    public ChatViewModel() {
        pendingRequestsCount.bind(Bindings.size(pendingInvitations));

        selectedChat.addListener((obs, oldChat, newChat) -> {
            if (newChat != null) {
                currentContact.set(newChat.chatName());
                chatTitle.set(newChat.chatName());
                contactEmail.set(newChat.chatName());
                fetchMessageHistory(newChat.chatId(), SessionManager.getUserId());
            } else {
                showInfoPane.set(false);
                chatTitle.set("Select a conversation");
                messageHistory.clear();
            }
        });
    }

    public void startPolling() {
        scheduler.scheduleAtFixedRate(this::updateChatList, 2, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::fetchPendingInvitations, 3, 5, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void updateChatList() {
        try {
            RemoteChatService chatService = ClientManager.getInstance().getRemoteChatService();
            List<ChatSummaryDTO> chats = chatService.getUserChats(SessionManager.getUserId());
            Platform.runLater(() -> {
                chatSummaryList.setAll(chats);
            });
        } catch (RemoteException e) {
            System.err.println("Error polling chat list: " + e.getMessage());
        }
    }

    public void loadUserChats(Long userId) {
        new Thread(() -> {
            try {
                RemoteChatService service = ClientManager.getInstance().getRemoteChatService();
                if (service != null) {
                    List<ChatSummaryDTO> chats = service.getUserChats(userId);
                    Platform.runLater(() -> {
                        chatSummaryList.setAll(chats);
                        System.out.println("Sidebar populated with " + chats + " chats.");
                    });
                }
            } catch (Exception e) {
                System.err.println("RMI Error during sidebar fetch: " + e.getMessage());
            }
        }).start();
    }

    private void fetchMessageHistory(Long chatId, long currentUserId) {
        new Thread(() -> {
            try {
                List<MessageDTO> history = ClientManager.getInstance().getRemoteChatService()
                        .getChatMessages(chatId, currentUserId);

                Platform.runLater(() -> {
                    messageHistory.setAll(history);
                });
            } catch (RemoteException e) {
                System.err.println("Error fetching message history for chat: " + chatId);
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty() || selectedChat.get() == null) {
            return;
        }

        String senderName = SessionManager.getDisplayName();
        MessageDTO newMsg = new MessageDTO(text, LocalDateTime.now(), true, senderName, this.selectedChat.get().chatId());

        new Thread(() -> {
            try {
                ClientManager.getInstance().getRemoteChatService()
                        .sendMessage(selectedChat.get().chatId(), text, SessionManager.getUserId());
                Platform.runLater(() -> {
                    messageHistory.add(newMsg);
                });
            } catch (RemoteException e) {
                System.err.println("Failed to send message via RMI");
                e.printStackTrace();
            }
        }).start();
    }

    public void showMyProfile() {
        selectedChat.set(null);
        currentContact.set("Omar Ahmed");
        contactEmail.set("omar@gmail.com");
        // Update active view to PROFILE if you have that enum, otherwise CONTACT_INFO
        activeRightView.set(RightPaneView.CONTACT_INFO);
    }

    public void showInvitations() {
        fetchInvitationsFromServer();
        activeRightView.set(RightPaneView.INVITATIONS);
    }

    public ObservableList<InvitationDTO> getPendingInvitations() {
        return pendingInvitations;
    }

    public void fetchInvitationsFromServer() {
        fetchPendingInvitations();
    }

    private void fetchPendingInvitations() {
        new Thread(() -> {
            try {
                RemoteContactsService service = ClientManager.getInstance()
                        .getRemoteContactsService();
                if (service != null) {
                    List<InvitationDTO> list = service
                            .getPendingRequests(SessionManager.getUserId());
                    Platform.runLater(() -> pendingInvitations.setAll(list));
                }
            } catch (RemoteException e) {
                System.err.println("Error fetching invitations: " + e.getMessage());
            }
        }).start();
    }

    public void showAddContact() {
        activeRightView.set(RightPaneView.ADD_CONTACT);
        showSettingsPane.set(false);
        showInfoPane.set(true);
        openInfoPane();
    }

    public void showSettings() {
        selectedChat.set(null);
        showInfoPane.set(false);
        showSettingsPane.set(true);
    }

    public void closeSettingsPane() {
        showSettingsPane.set(false);
    }

    public void toggleInfoPane() {
        if (activeRightView.get() != RightPaneView.NONE) {
            activeRightView.set(RightPaneView.NONE);
        } else {
            activeRightView.set(RightPaneView.CONTACT_INFO);
        }
    }

    public void openInfoPane() {
        activeRightView.set(RightPaneView.CONTACT_INFO);
    }

    public UserDTO search(String email) throws RemoteException {
        return ClientManager.getInstance().getRemoteUserService().getUserByEmail(email);
    }

    public void logout() {
        new Thread(() -> {
            try {
                long userId = SessionManager.getUserId();
                ClientManager.getInstance().getRemoteUserService().logout(userId, "");

                Platform.runLater(() -> {
                    SessionManager.clearSession();
                    SceneManager.getInstance().showLoginScreen();
                });
            } catch (RemoteException e) {
                System.err.println("Failed to logout via RMI");
                e.printStackTrace();
            }
        }).start();
    }

    public ObservableList<ChatSummaryDTO> getChatSummaryList() {
        return chatSummaryList;
    }

    public ObjectProperty<ChatSummaryDTO> selectedChatProperty() {
        return selectedChat;
    }

    public ObservableList<MessageDTO> getMessageHistory() {
        return messageHistory;
    }

    public ObjectProperty<String> currentContactProperty() {
        return currentContact;
    }

    public StringProperty chatTitleProperty() {
        return chatTitle;
    }

    public StringProperty contactEmailProperty() {
        return contactEmail;
    }

    public BooleanProperty showInfoPaneProperty() {
        return showInfoPane;
    }

    public ObjectProperty<RightPaneView> activeRightViewProperty() {
        return activeRightView;
    }

    public IntegerProperty pendingRequestsCountProperty() {
        return pendingRequestsCount;
    }

    public IntegerProperty unreadAnnouncementsCountProperty() {
        return unreadAnnouncementsCount;
    }

    public void sendInvitation(long id) throws RemoteException {
        ClientManager.getInstance().getRemoteContactsService()
                .sendRequest(SessionManager.getUserId(), id);
    }

    public void acceptInvitation(InvitationDTO invitation) throws RemoteException {
        new Thread(() -> {
            try {
                ClientManager.getInstance().getRemoteContactsService()
                        .acceptRequest(SessionManager.getUserId(), invitation.ownerId());
                Platform.runLater(() -> {
                    pendingInvitations.remove(invitation);
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void declineInvitation(InvitationDTO invitation) throws RemoteException {
        new Thread(() -> {
            try {
                ClientManager.getInstance().getRemoteContactsService()
                        .declineRequest(SessionManager.getUserId(), invitation.ownerId());
                Platform.runLater(() -> pendingInvitations.remove(invitation));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void changeStatus(UserStatus status) {
        new Thread(() -> {
            try {
                ClientManager.getInstance().getRemoteUserService()
                        .updateStatus(SessionManager.getUserId(), status);
            } catch (RemoteException e) {
                System.err.println("Failed to update user status: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public BooleanProperty showSettingsPaneProperty() {
        return showSettingsPane;
    }

    public BooleanProperty enableSystemNotificationsProperty() {
        return enableSystemNotifications;
    }

    public boolean isSystemNotificationsEnabled() {
        return enableSystemNotifications.get();
    }
}
