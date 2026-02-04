package com.jets.chat.server.rmi;

import com.jets.chat.common.dto.InvitationDTO;
import com.jets.chat.common.rmi.RemoteContactsService;
import com.jets.chat.server.service.ContactsService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RemoteContactsServiceImpl extends UnicastRemoteObject
        implements
            RemoteContactsService {

    private final ContactsService contactsService;

    public RemoteContactsServiceImpl(ContactsService contactsService) throws RemoteException {
        this.contactsService = contactsService;
    }

    @Override
    public List<InvitationDTO> getPendingRequests(long userId) throws RemoteException {
        return contactsService.getPendingRequests(userId);
    }

    @Override
    public void sendRequest(long fromId, long toId) throws RemoteException {
        contactsService.sendRequest(fromId, toId);
    }

    @Override
    public void acceptRequest(long contactId, long ownerId) throws RemoteException {
        contactsService.acceptRequest(contactId, ownerId);
    }

    @Override
    public void declineRequest(long contactId, long ownerId) throws RemoteException {
        contactsService.declineRequest(contactId, ownerId);
    }
}
