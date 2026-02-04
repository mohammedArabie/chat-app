package com.jets.chat.server.service;

import com.jets.chat.common.dto.InvitationDTO;
import com.jets.chat.server.entity.Contact;

import java.rmi.RemoteException;
import java.util.List;

public interface ContactsService {

    List<InvitationDTO> getPendingRequests(long userId);

    void sendRequest(long fromId, long toId);

    void acceptRequest(long contactId, long ownerId) throws RemoteException;

    void declineRequest(long contactId, long ownerId);

    List<Contact> getContacts(long userId);
}
