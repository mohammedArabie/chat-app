package com.jets.chat.common.callback;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException;

    /**
     * Called by the server when a new message is sent to the logged-in user.
     */
    void receiveMessage(MessageDTO message) throws RemoteException;

    void updateContactStatus(Long contactId, UserStatus status) throws RemoteException;

    void reloadChats() throws RemoteException;
}