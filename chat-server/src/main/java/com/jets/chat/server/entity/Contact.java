package com.jets.chat.server.entity;

import com.jets.chat.common.enums.ContactStatus;

import java.sql.Timestamp;

public class Contact {

    private long ownerId;
    private long contactId;
    private ContactStatus status;
    private String category;
    private Timestamp createdAt;

    public Contact() {
    }

    public Contact(long ownerId, long contactId, ContactStatus status, String category,
            Timestamp createdAt) {
        this.ownerId = ownerId;
        this.contactId = contactId;
        this.status = status;
        this.category = category;
        this.createdAt = createdAt;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public ContactStatus getStatus() {
        return status;
    }

    public void setStatus(ContactStatus status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
