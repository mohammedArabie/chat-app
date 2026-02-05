package com.jets.chat.common.rmi;

import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.MessageDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteChatService extends Remote {

    List<ChatSummaryDTO> getUserChats(Long userId) throws RemoteException;

    List<MessageDTO> getChatMessages(Long chatId, long currentUserId) throws RemoteException;

    void sendMessage(Long chatId, String content, long currentUserId) throws RemoteException;

    void sendFileMessage(Long chatId, String fileName, byte[] fileData, String contentType,
            long fileSize, long currentUserId) throws RemoteException;

    long createGroup(String groupName, long ownerId, List<Long> memberIds) throws RemoteException;

    boolean isUserInChat(long userId, long chatId) throws RemoteException;
}