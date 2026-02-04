package com.jets.chat.server.service.impl;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.ChatType;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.ChatDao;
import com.jets.chat.server.dao.MessageDao;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatParticipant;
import com.jets.chat.server.entity.Message;
import com.jets.chat.server.entity.User;
import com.jets.chat.server.service.ChatService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatServiceImpl implements ChatService {

    private final ChatDao chatDao;
    private final MessageDao messageDao;
    private final UserDao userDao;

    public ChatServiceImpl(ChatDao chatDao, MessageDao messageDao, UserDao userDao) {
        this.chatDao = chatDao;
        this.messageDao = messageDao;
        this.userDao = userDao;
    }

    @Override
    public List<ChatSummaryDTO> getUserChats(Long userId) {
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
                            long otherUserId = chatParticipant.getUserId();
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
    public List<MessageDTO> getChatMessages(Long chatId, long currentUserId) {
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
    public void sendMessage(Long chatId, String content, long currentUserId) {
        Message message = new Message();
        message.setChatId(chatId);
        message.setContent(content);
        message.setSenderId(currentUserId);
        message.setSentAt(LocalDateTime.now());
        messageDao.save(message);
        notifyParticipants(chatId, content, currentUserId);
    }

    @Override
    public void notifyParticipants(Long chatId, String content, long currentUserId) {
        List<ChatParticipant> participantIds = chatDao.getParticipants(chatId);

        var onlineClients = ServerManager.getInstance().getOnlineClients();

        for (ChatParticipant user : participantIds) {
            ClientCallback callback = onlineClients.get(user.getUserId());
            if (callback != null) {
                try {
                    callback.receiveMessage(
                            new MessageDTO(content, LocalDateTime.now(), user.getUserId() == currentUserId, null));
                } catch (Exception e) {
                    onlineClients.remove(user.getUserId());
                }
            }
        }
    }
}
