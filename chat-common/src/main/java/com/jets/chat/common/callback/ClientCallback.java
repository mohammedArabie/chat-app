package com.jets.chat.common.callback;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.dto.InvitationDTO;
import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.enums.UserStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException;

    void receiveMessage(MessageDTO message) throws RemoteException;

    void updateContactStatus(Long contactId, UserStatus status) throws RemoteException;

    void reloadChats() throws RemoteException;

    void onInvitationReceived(InvitationDTO invitation) throws RemoteException;

    void onInvitationAccepted(String contactName) throws RemoteException;
}