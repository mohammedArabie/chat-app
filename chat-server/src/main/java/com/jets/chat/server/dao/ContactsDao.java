package com.jets.chat.server.dao;

import com.jets.chat.common.entity.ContactStatus;
import com.jets.chat.server.entity.Contact;

import java.util.List;
import java.util.Optional;

public interface ContactsDao {

    // Create / Send friend request or add contact
    Contact save(Contact contact);

    // Read
    Optional<Contact> findByIds(long ownerId, long contactId);

    List<Contact> findAllContactsByOwnerId(long ownerId);

    List<Contact> findAllContactsByOwnerIdAndStatus(long ownerId, ContactStatus status);

    List<Contact> findAllContactsByOwnerIdAndCategory(long ownerId, String category);

    // Update (status/category)
    boolean update(Contact contact);

    boolean updateStatus(long ownerId, long contactId, ContactStatus status);

    boolean updateCategory(long ownerId, long contactId, String category);

    // Delete / Remove contact
    boolean deleteByIds(long ownerId, long contactId);

    // Helpers
    boolean exists(long ownerId, long contactId);
}
