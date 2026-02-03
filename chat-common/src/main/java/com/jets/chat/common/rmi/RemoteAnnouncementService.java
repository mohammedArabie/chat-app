package com.jets.chat.common.rmi;

import com.jets.chat.common.dto.AnnouncementDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteAnnouncementService extends Remote {
    List<AnnouncementDTO> getAllAnnouncements() throws RemoteException;

    List<AnnouncementDTO> getLatestAnnouncements(int fetchSize) throws RemoteException;

    AnnouncementDTO createAnnouncement(AnnouncementDTO announcementDTO) throws RemoteException;;

    int getActiveCallbackCount() throws RemoteException;
}
