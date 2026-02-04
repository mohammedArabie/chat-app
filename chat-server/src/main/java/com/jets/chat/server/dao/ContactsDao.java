package com.jets.chat.server.dao;

import com.jets.chat.common.enums.ContactStatus;
import com.jets.chat.server.entity.Contact;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

public interface ContactsDao {

    Contact save(Contact contact);

    Optional<Contact> findByIds(long ownerId, long contactId);

    List<Contact> findAllContactsByOwnerId(long ownerId);

    List<Contact> findAllContactsByOwnerIdAndStatus(long ownerId, ContactStatus status);

    List<Contact> findAllContactsByOwnerIdAndCategory(long ownerId, String category);

    boolean update(Contact contact);

    boolean updateStatus(long ownerId, long contactId, ContactStatus status);

    boolean updateCategory(long ownerId, long contactId, String category);

    boolean deleteByIds(long ownerId, long contactId);

    boolean exists(long ownerId, long contactId);

    List<Pair<Long, Long>> getPendingRequestsByContactId(long contactId);
}
