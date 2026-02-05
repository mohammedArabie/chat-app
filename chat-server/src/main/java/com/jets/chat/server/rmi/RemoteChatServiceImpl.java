package com.jets.chat.server.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.FileDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.ChatType;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.rmi.RemoteChatService;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.ChatDao;
import com.jets.chat.server.dao.FileDao;
import com.jets.chat.server.dao.MessageDao;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.dao.impl.ChatDaoImpl;
import com.jets.chat.server.dao.impl.FileDaoImpl;
import com.jets.chat.server.dao.impl.MessageDaoImpl;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.entity.*;
import com.jets.chat.server.service.ChatService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RemoteChatServiceImpl extends UnicastRemoteObject implements RemoteChatService {
    private static final String FILE_STORAGE_BASE = System.getProperty("user.home") + File.separator
            + "chat-server" + File.separator + "files";

    private final ChatDao chatDao = new ChatDaoImpl(DataSourceConfig.getDataSource());
    private final MessageDao messageDao = new MessageDaoImpl(DataSourceConfig.getDataSource());
    private final UserDao userDao = new UserDaoImpl(DataSourceConfig.getDataSource());
    private final FileDao fileDao = new FileDaoImpl(DataSourceConfig.getDataSource());

    private final ChatService chatService;

    public RemoteChatServiceImpl(ChatService chatService) throws RemoteException {
        super();
        this.chatService = chatService;
        new File(FILE_STORAGE_BASE).mkdirs();
    }

    @Override
    public List<ChatSummaryDTO> getUserChats(Long userId) throws RemoteException {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        UserStatus status = null;

        List<Chat> chats = chatDao.findChatsByUser(userId);
        List<ChatSummaryDTO> result = new ArrayList<>();
        for (Chat chat : chats) {
            Optional<Message> lastMessage = messageDao.findLatest(chat.getChatId());
            String chatName = "", lastMessageSender = null;
            if (chat.getChatType().equals(ChatType.GROUP)) {
                Optional<String> name = chatDao.findGroupNameByChatId(chat.getChatId());
                if (name.isEmpty())
                    throw new RuntimeException("group doesn't have a name");
                chatName = name.get();
                if (lastMessage.isPresent()) {
                    Optional<User> user = userDao.findById(lastMessage.get().getSenderId());
                    lastMessageSender = user.map(User::getDisplayName).orElse(null);
                }
                status = UserStatus.AVAILABLE;
            } else {
                if (lastMessage.isPresent()) {
                    Optional<User> user = userDao.findById(lastMessage.get().getSenderId());
                    lastMessageSender = user.map(User::getDisplayName).orElse(null);
                }
                List<ChatParticipant> participants = chatDao.getParticipants(chat.getChatId());
                for (ChatParticipant chatParticipant : participants) {
                    if (chatParticipant.getUserId() != userId) {
                        Optional<User> user = userDao.findById(chatParticipant.getUserId());
                        chatName = user.get().getDisplayName();
                        status = userDao.getStatus(chatParticipant.getUserId());
                    }
                }
            }
            result.add(new ChatSummaryDTO(chat.getChatId(), chatName,
                    lastMessage.<String>map(Message::getContent).orElse(null),
                    lastMessage.<LocalDateTime>map(Message::getSentAt).orElse(null),
                    lastMessageSender, status, chat.getChatType()));
        }
        return result;
    }

    @Override
    public List<MessageDTO> getChatMessages(Long chatId, long currentUserId)
            throws RemoteException {
        if (chatDao.findById(chatId).isEmpty()) {
            throw new RuntimeException("Chat not found");
        }

        List<MessageDTO> messages = new ArrayList<>();
        List<Message> messageList = messageDao.findByChatId(chatId);

        for (Message msg : messageList) {
            boolean isSentByMe = msg.getSenderId() == currentUserId;

            if ("FILE".equals(msg.getMessageType())) {
                Optional<FileMetadata> fileMetadata = fileDao.findByMessageId(msg.getMessageId());
                if (fileMetadata.isPresent()) {
                    FileMetadata fm = fileMetadata.get();
                    FileDTO fileDTO = new FileDTO(fm.getFileId(), fm.getFileName(),
                            fm.getFileSize(), fm.getContentType(), fm.getFilePath());
                    messages.add(new MessageDTO(chatId, msg.getContent(), msg.getSentAt(),
                            isSentByMe, fileDTO,
                            userDao.findById(msg.getSenderId()).get().getDisplayName()));
                } else {
                    messages.add(
                            new MessageDTO(chatId, msg.getContent(), msg.getSentAt(), isSentByMe,
                                    userDao.findById(msg.getSenderId()).get().getDisplayName()));
                }
            } else {
                messages.add(new MessageDTO(chatId, msg.getContent(), msg.getSentAt(), isSentByMe,
                        userDao.findById(msg.getSenderId()).get().getDisplayName()));
            }
        }

        return messages;
    }

    @Override
    public void sendMessage(Long chatId, String content, long currentUserId)
            throws RemoteException {
        Message message = new Message();
        message.setChatId(chatId);
        message.setContent(content);
        message.setSenderId(currentUserId);
        message.setSentAt(LocalDateTime.now());
        message.setMessageType("TEXT");
        messageDao.save(message);
        notifyParticipants(chatId, currentUserId, content, null);
    }

    @Override
    public void sendFileMessage(Long chatId, String fileName, byte[] fileData, String contentType,
            long fileSize, long currentUserId) throws RemoteException {
        try {
            Path chatDir = Paths.get(FILE_STORAGE_BASE, chatId.toString());
            Files.createDirectories(chatDir);

            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path filePath = chatDir.resolve(uniqueFileName);

            Files.write(filePath, fileData);

            Message message = new Message();
            message.setChatId(chatId);
            message.setContent(fileName);
            message.setSenderId(currentUserId);
            message.setSentAt(LocalDateTime.now());
            message.setMessageType("FILE");
            Message savedMessage = messageDao.save(message);

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setMessageId(savedMessage.getMessageId());
            fileMetadata.setFileName(fileName);
            fileMetadata.setFilePath(filePath.toString());
            fileMetadata.setFileSize(fileSize);
            fileMetadata.setContentType(contentType);
            FileMetadata savedFile = fileDao.save(fileMetadata);

            System.out.println(
                    "File saved with ID: " + savedFile.getFileId() + " at path: " + filePath);

            FileDTO fileDTO = new FileDTO(savedFile.getFileId(), savedFile.getFileName(),
                    savedFile.getFileSize(), savedFile.getContentType(), savedFile.getFilePath());

            notifyParticipants(chatId, currentUserId, fileName, fileDTO);

        } catch (IOException e) {
            throw new RemoteException("Failed to save file", e);
        }
    }

    @Override
    public boolean isUserInChat(long userId, long chatId) throws RemoteException {
        for (ChatParticipant chatParticipant : chatDao.getParticipants(chatId)) {
            if (chatParticipant.getUserId() == userId)
                return true;
        }
        return false;
    }

    @Override
    public long createGroup(String groupName, long ownerId, List<Long> chatIds)
            throws RemoteException {
        List<Long> userIds = new ArrayList<>();
        for (Long chatId : chatIds) {
            userIds.add(chatService.findChatUser(chatId, ownerId));
        }
        return chatService.createGroup(groupName, ownerId, userIds);
    }

    private void notifyParticipants(Long chatId, long senderId, String content, FileDTO fileDTO) {
        List<ChatParticipant> participantIds = chatDao.getParticipants(chatId);

        MessageDTO dto;
        if (fileDTO != null) {
            dto = new MessageDTO(chatId, content, LocalDateTime.now(), false, fileDTO,
                    userDao.findById(senderId).get().getDisplayName());
        } else {
            dto = new MessageDTO(chatId, content, LocalDateTime.now(), false,
                    userDao.findById(senderId).get().getDisplayName());
        }

        var onlineClients = ServerManager.getInstance().getOnlineClients();

        for (ChatParticipant user : participantIds) {
            // Don't send notification to the sender themselves
            if (user.getUserId() == senderId) {
                continue;
            }

            ClientCallback callback = onlineClients.get(user.getUserId());
            if (callback != null) {
                try {
                    callback.receiveMessage(dto);
                } catch (RemoteException e) {
                    System.err.println(
                            "Failed to notify user " + user.getUserId() + ": " + e.getMessage());
                    onlineClients.remove(user.getUserId());
                }
            }
        }
    }
}
