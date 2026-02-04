package com.jets.chat.common.dto;

import java.io.Serializable;

public record FileDTO(Long fileId, String fileName, long fileSize, String contentType,
        String filePath) implements Serializable {
}