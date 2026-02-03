package com.jets.chat.common.rmi;

import com.jets.chat.common.dto.AnnouncementDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Client implements this to receive real-time announcements Server calls this
 * method when admin sends announcement
 */
public interface AnnouncementCallback extends Remote {
    void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException;
}