package com.jets.chat.client.ui.viewmodel;

import com.jets.chat.client.util.ClientManager;
import com.jets.chat.client.util.SceneManager;
import com.jets.chat.client.util.SessionManager;
import com.jets.chat.common.dto.*;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatViewModel {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String CLIENT_FILE_STORAGE = System.getProperty("user.home")
            + File.separator + "chat-app";

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

        ensureFileStorageDirectory();
    }

    private void ensureFileStorageDirectory() {
        try {
            Path userDir = Paths.get(CLIENT_FILE_STORAGE,
                    String.valueOf(SessionManager.getUserId()), "files");
            Files.createDirectories(userDir);
        } catch (IOException e) {
            System.err.println("Failed to create file storage directory: " + e.getMessage());
        }
    }

    public void addMessage(MessageDTO messageDTO) {
        if (selectedChat.get() != null && selectedChat.get().chatId() == messageDTO.chatId()) {
            messageHistory.add(messageDTO);
        }
        for (int i = 0; i < chatSummaryList.size(); i++) {
            ChatSummaryDTO chat = chatSummaryList.get(i);
            if (chat.chatId() == messageDTO.chatId()) {
                chatSummaryList.set(i,
                        new ChatSummaryDTO(chat.chatId(), chat.chatName(), messageDTO.content(),
                                messageDTO.time(), messageDTO.senderName(), UserStatus.AVAILABLE));
                break;
            }
        }
    }

    public void updateChatList() {
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

        MessageDTO newMsg = new MessageDTO(selectedChat.get().chatId(), text, LocalDateTime.now(),
                true, SessionManager.getDisplayName());

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

    public void sendFileMessage(File file) {
        if (file == null || selectedChat.get() == null) {
            return;
        }

        if (file.length() > MAX_FILE_SIZE) {
            Platform.runLater(() -> {
                System.err.println("File too large. Maximum size is 50MB.");
            });
            return;
        }

        new Thread(() -> {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                String fileName = file.getName();
                String contentType = Files.probeContentType(file.toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                ClientManager.getInstance().getRemoteChatService().sendFileMessage(
                        selectedChat.get().chatId(), fileName, fileData, contentType, file.length(),
                        SessionManager.getUserId());

                FileDTO tempFileDTO = new FileDTO(null, // fileId will be assigned by server
                        fileName, file.length(), contentType, file.getAbsolutePath()

                );
                MessageDTO newMsg = new MessageDTO(selectedChat.get().chatId(), fileName,
                        LocalDateTime.now(), true, tempFileDTO, SessionManager.getDisplayName());

                Platform.runLater(() -> {
                    messageHistory.add(newMsg);
                });

                System.out.println("File sent successfully: " + fileName);

            } catch (IOException e) {
                System.err.println("Failed to send file: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.err.println("Failed to send file");
                });
            }
        }).start();
    }

    public void downloadAndOpenFile(FileDTO fileDTO) {
        if (fileDTO == null) {
            System.err.println("FileDTO is null");
            return;
        }

        new Thread(() -> {
            try {
                Path localFilePath;

                if (fileDTO.filePath() != null && !fileDTO.filePath().isEmpty()) {
                    Path originalPath = Paths.get(fileDTO.filePath());

                    if (Files.exists(originalPath)) {
                        localFilePath = originalPath;
                        System.out.println("Using original file path: " + localFilePath);
                    } else {
                        localFilePath = downloadFileFromServer(fileDTO);
                    }
                } else {
                    localFilePath = downloadFileFromServer(fileDTO);
                }

                if (localFilePath == null || !Files.exists(localFilePath)) {
                    System.err.println(
                            "File not found: " + (localFilePath != null ? localFilePath : "null"));
                    return;
                }

                final Path finalPath = localFilePath;
                Platform.runLater(() -> {
                    try {
                        openFileLocation(finalPath);
                    } catch (IOException e) {
                        System.err.println("Failed to open file location: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("Error in downloadAndOpenFile: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private Path downloadFileFromServer(FileDTO fileDTO) throws IOException, RemoteException {
        if (fileDTO.fileId() == null) {
            System.err.println("Cannot download file: fileId is null");
            return null;
        }

        Path userFilesDir = Paths.get(CLIENT_FILE_STORAGE,
                String.valueOf(SessionManager.getUserId()), "files");
        Files.createDirectories(userFilesDir);

        Path localFilePath = userFilesDir.resolve(fileDTO.fileName());

        if (!Files.exists(localFilePath)) {
            System.out.println("Downloading file from server, fileId: " + fileDTO.fileId());
            byte[] fileData = ClientManager.getInstance().getRemoteFileService()
                    .downloadFile(fileDTO.fileId());
            Files.write(localFilePath, fileData);
            System.out.println("File downloaded to: " + localFilePath);
        } else {
            System.out.println("File already exists locally: " + localFilePath);
        }

        return localFilePath;
    }

    private void openFileLocation(Path filePath) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;

        if (os.contains("win")) {
            pb = new ProcessBuilder("explorer.exe", "/select,", filePath.toString());
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("open", "-R", filePath.toString());
        } else {
            pb = new ProcessBuilder("xdg-open", filePath.getParent().toString());
        }

        pb.start();
        System.out.println("Opened file location: " + filePath);
    }

    public void updateChatInSidebar(MessageDTO message) {
        chatSummaryList.stream().filter(c -> c.chatId() == message.chatId()).findFirst()
                .ifPresent(oldChat -> {
                    ChatSummaryDTO updatedChat = new ChatSummaryDTO(oldChat.chatId(),
                            oldChat.chatName(), message.content(), message.time(),
                            message.senderName(), UserStatus.AVAILABLE);

                    int index = chatSummaryList.indexOf(oldChat);
                    chatSummaryList.remove(index);
                    chatSummaryList.add(0, updatedChat); // Move to the very top
                });
    }

    public void showMyProfile() {
        selectedChat.set(null);
        currentContact.set("Omar Ahmed");
        contactEmail.set("omar@gmail.com");
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

    public void sendInvitation(long id) throws RemoteException {
        ClientManager.getInstance().getRemoteContactsService()
                .sendRequest(SessionManager.getUserId(), id);
    }

    public void acceptInvitation(InvitationDTO invitation) throws RemoteException {
        new Thread(() -> {
            try {
                ClientManager.getInstance().getRemoteContactsService()
                        .acceptRequest(SessionManager.getUserId(), invitation.ownerId());
                updateChatList();
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
