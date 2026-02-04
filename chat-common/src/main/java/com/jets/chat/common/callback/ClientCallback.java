package com.jets.chat.common.callback;

import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.dto.AnnouncementDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The RMI Callback interface. This is implemented by the CLIENT so the SERVER
 * can push live updates.
 */
public interface ClientCallback extends Remote {
    void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException;

    /**
     * Called by the server when a new message is sent to the logged-in user.
     */
    void receiveMessage(MessageDTO message) throws RemoteException;

    /**
     * Called by the server when one of the user's contacts changes their status
     * (e.g., Sarah Chen goes from Online to Away).
     */
    void updateContactStatus(Integer contactId, UserStatus status) throws RemoteException;

}