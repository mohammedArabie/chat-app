package com.jets.chat.server.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.ChatType;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.rmi.RemoteChatService;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.ChatDao;
import com.jets.chat.server.dao.MessageDao;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.dao.impl.ChatDaoImpl;
import com.jets.chat.server.dao.impl.MessageDaoImpl;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatParticipant;
import com.jets.chat.server.entity.Message;
import com.jets.chat.server.entity.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RemoteChatServiceImpl extends UnicastRemoteObject implements RemoteChatService {
    private final ChatDao chatDao = new ChatDaoImpl(DataSourceConfig.getDataSource());
    private final MessageDao messageDao = new MessageDaoImpl(DataSourceConfig.getDataSource());
    private final UserDao userDao = new UserDaoImpl(DataSourceConfig.getDataSource());

    public RemoteChatServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<ChatSummaryDTO> getUserChats(Long userId) throws RemoteException {
        if (userDao.findById(userId).isEmpty()) {
            throw new RuntimeException("User not found");
        }
        List<Chat> chats = chatDao.findChatsByUser(userId);
        List<ChatSummaryDTO> result = new ArrayList<>();
        for (Chat chat : chats) {
            Optional<Message> lastMessage = messageDao.findLatest(chat.getChatId());
            String chatName = "", lastMessageSender = null;
            UserStatus status = UserStatus.OFFLINE;
            if (chat.getChatType().equals(ChatType.GROUP)) {
                Optional<String> name = chatDao.findGroupNameByChatId(chat.getChatId());
                if (name.isEmpty())
                    throw new RuntimeException("group doesn't have a name");
                chatName = name.get();
                if (lastMessage.isPresent()) {
                    Optional<User> user = userDao.findById(lastMessage.get().getSenderId());
                    lastMessageSender = user.map(User::getDisplayName).orElse(null);
                }
            } else {
                long otherUserId = -1;
                if (lastMessage.isPresent()) {
                    Optional<User> user = userDao.findById(lastMessage.get().getSenderId());
                    lastMessageSender = user.map(User::getDisplayName).orElse(null);
                }
                {
                    List<ChatParticipant> participants = chatDao.getParticipants(chat.getChatId());
                    System.out.println("participants " + participants);
                    for (ChatParticipant chatParticipant : participants) {
                        if (chatParticipant.getUserId() != userId) {
                            Optional<User> user = userDao.findById(chatParticipant.getUserId());
                            chatName = user.map(User::getDisplayName).orElse(null);
                            otherUserId = chatParticipant.getUserId();
                            status = userDao.getStatus(otherUserId);
                            break;
                        }
                    }
                }
            }
            result.add(new ChatSummaryDTO(chat.getChatId(), chatName,
                    lastMessage.<String>map(Message::getContent).orElse(null),
                    lastMessage.<LocalDateTime>map(Message::getSentAt).orElse(null),
                    lastMessageSender, status));
        }
        return result;
    }

    @Override
    public List<MessageDTO> getChatMessages(Long chatId, long currentUserId)
            throws RemoteException {
        if (chatDao.findById(chatId).isEmpty()) {
            throw new RuntimeException("Chat not found");
        }
        return messageDao.findByChatId(chatId).stream().map(e -> {
            Optional<User> sender = userDao.findById(e.getSenderId());
            String senderName = sender.map(User::getDisplayName).orElse("Unknown");
            return new MessageDTO(e.getContent(), e.getSentAt(), e.getSenderId() == currentUserId,
                    senderName);
        }).toList();
    }

    @Override
    public void sendMessage(Long chatId, String content, long currentUserId)
            throws RemoteException {
        Message message = new Message();
        message.setChatId(chatId);
        message.setContent(content);
        message.setSenderId(currentUserId);
        message.setSentAt(LocalDateTime.now());
        messageDao.save(message);
        notifyParticipants(chatId, content);
    }

    private void notifyParticipants(Long chatId, String content) {
        List<ChatParticipant> participantIds = chatDao.getParticipants(chatId);

        var onlineClients = ServerManager.getInstance().getOnlineClients();

        for (ChatParticipant user : participantIds) {
            ClientCallback callback = onlineClients.get(user.getUserId());
            if (callback != null) {
                try {
                    callback.receiveMessage(
                            new MessageDTO(content, LocalDateTime.now(), false, null));
                } catch (RemoteException e) {
                    onlineClients.remove(user.getUserId());
                }
            }
        }
    }
}