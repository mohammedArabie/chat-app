package com.jets.chat.common.rmi;

import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.MessageDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteChatService extends Remote {
    void registerClient(Long userId, RemoteClientService clientCallback) throws RemoteException;

    void unregisterClient(Long userId) throws RemoteException;

    List<ChatSummaryDTO> getUserChats(Long userId) throws RemoteException;

    List<MessageDTO> getChatMessages(Long chatId) throws RemoteException;

    void sendMessage(Long chatId, String content) throws RemoteException;
}