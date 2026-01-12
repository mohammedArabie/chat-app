package com.jets.chat.server.dao;

import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatGroup;
import com.jets.chat.server.entity.ChatParticipant;
import com.jets.chat.server.entity.ChatType;

import java.util.List;
import java.util.Optional;

public interface ChatDao {
    // Chat creation
    long insertChat(ChatType type);

    void insertGroup(long chatId, String groupName, long ownerId);

    // Chat retrieval
    Optional<Chat> findById(long chatId);

    List<Chat> findChatsByUser(long userId);

    // Participants
    boolean addParticipant(long chatId, long userId);

    boolean removeParticipant(long chatId, long userId);

    List<ChatParticipant> getParticipants(long chatId);

    // Group operations
    Optional<ChatGroup> getGroupInfo(long chatId);

    boolean renameGroup(long chatId, String newName);

    boolean changeGroupOwner(long chatId, long newOwnerId);
}
