package com.jets.chat.server.entity;

public class FileMetadata {
    private Long fileId;
    private Long messageId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;

    public FileMetadata() {
    }

    public FileMetadata(Long fileId, Long messageId, String fileName, String filePath,
            Long fileSize, String contentType) {
        this.fileId = fileId;
        this.messageId = messageId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}