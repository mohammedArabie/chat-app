package com.jets.chat.server.dao;

import com.jets.chat.server.dto.CreateMessageDto;
import com.jets.chat.server.dto.MessageResponseDto;
import com.jets.chat.server.dto.EditMessageDto;

import java.util.List;
import java.util.Optional;

public interface MessageDao {
    MessageResponseDto save(CreateMessageDto messageDto);

    List<MessageResponseDto> findAll();

    Optional<MessageResponseDto> findById(long id);

    MessageResponseDto update(EditMessageDto messageDto);

    MessageResponseDto deleteById(long id);
}
