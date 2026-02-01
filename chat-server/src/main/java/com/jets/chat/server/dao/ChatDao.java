package com.jets.chat.server.dao;

import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatGroup;
import com.jets.chat.server.entity.ChatParticipant;
import com.jets.chat.common.enums.ChatType;

import java.util.List;
import java.util.Optional;

public interface ChatDao {
    long insertChat(ChatType type);

    boolean insertGroup(long chatId, String groupName, long ownerId);

    Optional<Chat> findById(long chatId);

    List<Chat> findChatsByUser(long userId);

    boolean addParticipant(long chatId, long userId);

    boolean removeParticipant(long chatId, long userId);

    List<ChatParticipant> getParticipants(long chatId);

    Optional<ChatGroup> getGroupInfo(long chatId);

    boolean renameGroup(long chatId, String newName);

    boolean changeGroupOwner(long chatId, long newOwnerId);
}
