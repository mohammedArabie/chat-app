package com.jets.chat.server.service;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.rmi.AnnouncementCallback;

import java.rmi.RemoteException;
import java.util.List;

public interface AnnouncementService {
    List<AnnouncementDTO> getAllAnnouncements();

    List<AnnouncementDTO> getLatestAnnouncements(int fetchSize);

    AnnouncementDTO createAnnouncement(AnnouncementDTO announcementDTO);

    void registerCallback(String sessionId, AnnouncementCallback callback) throws RemoteException;
    void unregisterCallback(String sessionId) throws RemoteException;
    int getActiveCallbackCount();
    void clearCallbacks();
}
