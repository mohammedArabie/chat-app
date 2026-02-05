package com.jets.chat.server.dao;

import com.jets.chat.server.entity.FileMetadata;

import java.util.Optional;

public interface FileDao {
    FileMetadata save(FileMetadata file);

    Optional<FileMetadata> findById(long fileId);

    Optional<FileMetadata> findByMessageId(long messageId);

    boolean deleteById(long fileId);
}