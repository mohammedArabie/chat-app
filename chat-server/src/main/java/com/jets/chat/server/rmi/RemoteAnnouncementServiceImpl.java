package com.jets.chat.server.rmi;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.server.service.AnnouncementService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RemoteAnnouncementServiceImpl extends UnicastRemoteObject
        implements
            RemoteAnnouncementService {
    private final AnnouncementService announcementService;

    public RemoteAnnouncementServiceImpl(AnnouncementService announcementService)
            throws RemoteException {
        this.announcementService = announcementService;
    }

    @Override
    public List<AnnouncementDTO> getAllAnnouncements() throws RemoteException {
        return announcementService.getAllAnnouncements();
    }

    @Override
    public List<AnnouncementDTO> getLatestAnnouncements(int fetchSize) throws RemoteException {
        return announcementService.getLatestAnnouncements(fetchSize);
    }
}
