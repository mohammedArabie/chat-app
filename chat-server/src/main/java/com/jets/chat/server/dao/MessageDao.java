package com.jets.chat.server.dao;

import com.jets.chat.server.entity.Message;

import java.util.List;
import java.util.Optional;

public interface MessageDao {
    Message save(Message messageDto);

    List<Message> findByChatId(long chatId);

    Optional<Message> findById(long id);

    Optional<Message> findLatest(long chatId);

    Message update(Message messageDto);

    Optional<Message> deleteById(long id);
}
