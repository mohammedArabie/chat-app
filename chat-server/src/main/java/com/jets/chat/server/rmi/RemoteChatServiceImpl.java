package com.jets.chat.server.rmi;

import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.rmi.RemoteChatService;
import com.jets.chat.server.service.ChatService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RemoteChatServiceImpl extends UnicastRemoteObject implements RemoteChatService {

    private final ChatService chatService;

    public RemoteChatServiceImpl(ChatService chatService) throws RemoteException {
        super();
        this.chatService = chatService;
    }

    @Override
    public List<ChatSummaryDTO> getUserChats(Long userId) throws RemoteException {
        return chatService.getUserChats(userId);
    }

    @Override
    public List<MessageDTO> getChatMessages(Long chatId, long currentUserId)
            throws RemoteException {
        return chatService.getChatMessages(chatId, currentUserId);
    }

    @Override
    public void sendMessage(Long chatId, String content, long currentUserId)
            throws RemoteException {
        chatService.sendMessage(chatId, content, currentUserId);
    }
}
