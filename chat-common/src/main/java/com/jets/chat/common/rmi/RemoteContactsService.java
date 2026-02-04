package com.jets.chat.common.rmi;

import com.jets.chat.common.dto.InvitationDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteContactsService extends Remote {

    List<InvitationDTO> getPendingRequests(long userId) throws RemoteException;

    void sendRequest(long fromId, long toId) throws RemoteException;

    void acceptRequest(long contactId, long ownerId) throws RemoteException;

    void declineRequest(long contactId, long ownerId) throws RemoteException;
}
