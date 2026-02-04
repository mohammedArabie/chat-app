package com.jets.chat.server.service;

import com.jets.chat.common.dto.ChatSummaryDTO;
import com.jets.chat.common.dto.MessageDTO;

import java.util.List;

public interface ChatService {

    List<ChatSummaryDTO> getUserChats(Long userId);

    List<MessageDTO> getChatMessages(Long chatId, long currentUserId);

    void sendMessage(Long chatId, String content, long currentUserId);

    void notifyParticipants(Long chatId, String content, long currentUserId);
}
