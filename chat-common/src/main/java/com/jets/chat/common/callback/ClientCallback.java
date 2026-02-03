package com.jets.chat.common.callback;

import com.jets.chat.common.dto.AnnouncementDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException;
}