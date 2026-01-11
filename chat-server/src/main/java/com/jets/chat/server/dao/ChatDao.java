package com.jets.chat.server.dao;

import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatGroup;
import com.jets.chat.server.entity.ChatParticipant;

import java.util.List;
import java.util.Optional;

public interface ChatDao {
    // Chat creation
    Chat createPrivateChat(long user1Id, long user2Id);

    Chat createGroupChat(String groupName, long ownerId, List<Long> participantIds);

    // Chat retrieval
    Optional<Chat> findById(long chatId);

    List<Chat> findChatsByUser(long userId);

    // Participants
    void addParticipant(long chatId, long userId);

    void removeParticipant(long chatId, long userId);

    List<ChatParticipant> getParticipants(long chatId);

    // Group operations
    Optional<ChatGroup> getGroupInfo(long chatId);

    void renameGroup(long chatId, String newName);

    void changeGroupOwner(long chatId, long newOwnerId);
}
