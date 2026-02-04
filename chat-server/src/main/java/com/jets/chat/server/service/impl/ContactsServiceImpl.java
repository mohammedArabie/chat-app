package com.jets.chat.server.service.impl;

import com.jets.chat.common.dto.InvitationDTO;
import com.jets.chat.common.enums.ChatType;
import com.jets.chat.common.enums.ContactStatus;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.ChatDao;
import com.jets.chat.server.dao.ContactsDao;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.entity.Contact;
import com.jets.chat.server.entity.User;
import com.jets.chat.server.service.ContactsService;
import javafx.util.Pair;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContactsServiceImpl implements ContactsService {

    private final ContactsDao contactsDao;
    private final UserDao userDao;
    private final ChatDao chatDao;

    public ContactsServiceImpl(ContactsDao contactsDao, UserDao userDao, ChatDao chatDao) {
        this.contactsDao = contactsDao;
        this.userDao = userDao;
        this.chatDao = chatDao;
    }

    @Override
    public List<InvitationDTO> getPendingRequests(long userId) {
        List<Pair<Long, Long>> pendingContacts = contactsDao.getPendingRequestsByContactId(userId);
        List<InvitationDTO> requests = new ArrayList<>();
        for (Pair<Long, Long> contact : pendingContacts) {
            Optional<User> user = userDao.findById(contact.getValue());
            if (user.isPresent()) {
                Optional<Contact> contactRecord = contactsDao.findByIds(contact.getValue(),
                        contact.getKey());
                LocalDateTime createdAt = contactRecord.map(Contact::getCreatedAt)
                        .map(Timestamp::toLocalDateTime).orElse(LocalDateTime.now());
                requests.add(new InvitationDTO(contact.getValue(), user.get().getDisplayName(),
                        createdAt));
            }
        }
        return requests;
    }

    @Override
    public void sendRequest(long fromId, long toId) {
        if (contactsDao.exists(fromId, toId)) {
            throw new RuntimeException("Contact already exists");
        }
        Contact contact = new Contact();
        contact.setOwnerId(fromId);
        contact.setContactId(toId);
        contact.setStatus(ContactStatus.PENDING);
        contact.setCategory("Friends");
        contactsDao.save(contact);
    }

    @Override
    public void acceptRequest(long contactId, long ownerId) throws RemoteException {
        if (contactsDao.findByIds(ownerId, contactId).isEmpty()) {
            throw new RuntimeException("Contact not found");
        }
        if (contactsDao.updateStatus(ownerId, contactId, ContactStatus.ACCEPTED)) {
            long chatId = chatDao.insertChat(ChatType.PRIVATE);
            chatDao.addParticipant(chatId, ownerId);
            chatDao.addParticipant(chatId, contactId);
            ServerManager.getInstance().getOnlineClients().get(ownerId).reloadChats();
            ServerManager.getInstance().getOnlineClients().get(contactId).reloadChats();
        }
    }

    @Override
    public void declineRequest(long contactId, long ownerId) {
        contactsDao.deleteByIds(ownerId, contactId);
    }

    @Override
    public List<Contact> getContacts(long userId) {
        return contactsDao.findAllContactsByOwnerIdAndStatus(userId, ContactStatus.ACCEPTED);
    }
}
