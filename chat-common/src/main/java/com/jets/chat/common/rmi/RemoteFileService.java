package com.jets.chat.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteFileService extends Remote {

    /**
     * Upload file data to server
     * 
     * @param chatId
     *            The chat ID
     * @param fileName
     *            Original file name
     * @param fileData
     *            The file bytes
     * @param contentType
     *            MIME type
     * @return The file path on server
     */
    String uploadFile(Long chatId, String fileName, byte[] fileData, String contentType)
            throws RemoteException;

    /**
     * Download file data from server
     * 
     * @param fileId
     *            The file ID
     * @return The file bytes
     */
    byte[] downloadFile(Long fileId) throws RemoteException;
}