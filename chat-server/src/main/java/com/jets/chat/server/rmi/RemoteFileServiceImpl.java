package com.jets.chat.server.rmi;

import com.jets.chat.common.rmi.RemoteFileService;
import com.jets.chat.server.dao.FileDao;
import com.jets.chat.server.entity.FileMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;
import java.util.UUID;

public class RemoteFileServiceImpl extends UnicastRemoteObject implements RemoteFileService {

    private static final String FILE_STORAGE_BASE = System.getProperty("user.home") + File.separator
            + "chat-server" + File.separator + "files";

    private final FileDao fileDao;

    public RemoteFileServiceImpl(FileDao fileDao) throws RemoteException {
        super();
        this.fileDao = fileDao;
        // Ensure base directory exists
        new File(FILE_STORAGE_BASE).mkdirs();
    }

    @Override
    public String uploadFile(Long chatId, String fileName, byte[] fileData, String contentType)
            throws RemoteException {
        try {
            // Create chat-specific directory
            Path chatDir = Paths.get(FILE_STORAGE_BASE, chatId.toString());
            Files.createDirectories(chatDir);

            // Generate unique file name to avoid conflicts
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path filePath = chatDir.resolve(uniqueFileName);

            // Write file
            Files.write(filePath, fileData);

            return filePath.toString();

        } catch (IOException e) {
            throw new RemoteException("Failed to upload file", e);
        }
    }

    @Override
    public byte[] downloadFile(Long fileId) throws RemoteException {
        try {
            Optional<FileMetadata> fileMetadata = fileDao.findById(fileId);

            if (fileMetadata.isEmpty()) {
                throw new RemoteException("File not found");
            }

            Path filePath = Paths.get(fileMetadata.get().getFilePath());

            if (!Files.exists(filePath)) {
                throw new RemoteException("File does not exist on server");
            }

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            throw new RemoteException("Failed to download file", e);
        }
    }
}