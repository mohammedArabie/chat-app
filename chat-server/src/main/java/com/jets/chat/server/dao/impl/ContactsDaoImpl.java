package com.jets.chat.server.dao.impl;

import com.jets.chat.common.enums.ContactStatus;
import com.jets.chat.server.dao.ContactsDao;
import com.jets.chat.server.entity.Contact;
import com.zaxxer.hikari.HikariDataSource;
import javafx.util.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ContactsDaoImpl implements ContactsDao {

    private static final String INSERT_SQL = "INSERT INTO contacts (owner_id, contact_id, status, category, created_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_IDS_SQL = "SELECT * FROM contacts WHERE owner_id = ? AND contact_id = ?";
    private static final String SELECT_USER_CONTACTS = "SELECT * FROM contacts WHERE owner_id = ? OR contact_id = ? ORDER BY created_at DESC";
    private static final String SELECT_ALL_BY_OWNER_SQL = "SELECT * FROM contacts WHERE owner_id = ? ORDER BY created_at DESC";
    private static final String SELECT_ALL_BY_OWNER_AND_STATUS_SQL = "SELECT * FROM contacts WHERE owner_id = ? AND status = ? ORDER BY created_at DESC";
    private static final String SELECT_ALL_BY_OWNER_AND_CATEGORY_SQL = "SELECT * FROM contacts WHERE owner_id = ? AND category = ? ORDER BY created_at DESC";
    private static final String UPDATE_SQL = "UPDATE contacts SET status = ?, category = ? WHERE owner_id = ? AND contact_id = ?";
    private static final String UPDATE_STATUS_SQL = "UPDATE contacts SET status = ? WHERE owner_id = ? AND contact_id = ?";
    private static final String UPDATE_CATEGORY_SQL = "UPDATE contacts SET category = ? WHERE owner_id = ? AND contact_id = ?";
    private static final String DELETE_SQL = "DELETE FROM contacts WHERE owner_id = ? AND contact_id = ?";
    private static final String EXISTS_SQL = "SELECT 1 FROM contacts WHERE owner_id = ? AND contact_id = ? LIMIT 1";
    private static final String GET_PENDING_BY_REQUEST_ID_SQL = "SELECT * FROM contacts WHERE status = 'PENDING' AND contact_id = ?";
    private final HikariDataSource dataSource;

    public ContactsDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static Contact mapRowToContact(ResultSet rs) throws SQLException {
        Contact contact = new Contact();
        contact.setOwnerId(rs.getLong("owner_id"));
        contact.setContactId(rs.getLong("contact_id"));

        String statusStr = rs.getString("status");
        contact.setStatus(ContactStatus.valueOf(statusStr));

        contact.setCategory(rs.getString("category"));
        contact.setCreatedAt(rs.getTimestamp("created_at"));
        return contact;
    }

    @Override
    public Contact save(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null");
        }
        if (contact.getOwnerId() <= 0) {
            throw new IllegalArgumentException("ownerId must be > 0");
        }
        if (contact.getContactId() <= 0) {
            throw new IllegalArgumentException("contactId must be > 0");
        }
        if (contact.getStatus() == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, contact.getOwnerId());
            statement.setLong(2, contact.getContactId());

            statement.setString(3, contact.getStatus().name());

            statement.setString(4, contact.getCategory());

            if (contact.getCreatedAt() == null) {
                contact.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            }
            statement.setTimestamp(5, contact.getCreatedAt());

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Couldn't save to the database: NO ROWS AFFECTED");
            }

            return contact;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Contact> findByIds(long ownerId, long contactId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_IDS_SQL)) {

            statement.setLong(1, ownerId);
            statement.setLong(2, contactId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToContact(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Contact> findUserContacts(long userId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SELECT_USER_CONTACTS)) {

            statement.setLong(1, userId);
            statement.setLong(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Contact> contacts = new ArrayList<>();
                while (rs.next()) {
                    contacts.add(mapRowToContact(rs));
                }
                return contacts;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Contact> findAllContactsByOwnerId(long ownerId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SELECT_ALL_BY_OWNER_SQL)) {

            statement.setLong(1, ownerId);

            try (ResultSet rs = statement.executeQuery()) {
                List<Contact> contacts = new ArrayList<>();
                while (rs.next()) {
                    contacts.add(mapRowToContact(rs));
                }
                return contacts;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Contact> findAllContactsByOwnerIdAndStatus(long ownerId, ContactStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SELECT_ALL_BY_OWNER_AND_STATUS_SQL)) {

            statement.setLong(1, ownerId);
            statement.setString(2, status.name());

            try (ResultSet rs = statement.executeQuery()) {
                List<Contact> contacts = new ArrayList<>();
                while (rs.next()) {
                    contacts.add(mapRowToContact(rs));
                }
                return contacts;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Contact> findAllContactsByOwnerIdAndCategory(long ownerId, String category) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SELECT_ALL_BY_OWNER_AND_CATEGORY_SQL)) {

            statement.setLong(1, ownerId);
            statement.setString(2, category);

            try (ResultSet rs = statement.executeQuery()) {
                List<Contact> contacts = new ArrayList<>();
                while (rs.next()) {
                    contacts.add(mapRowToContact(rs));
                }
                return contacts;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null");
        }
        if (contact.getStatus() == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, contact.getStatus().name());
            statement.setString(2, contact.getCategory());
            statement.setLong(3, contact.getOwnerId());
            statement.setLong(4, contact.getContactId());

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateStatus(long ownerId, long contactId, ContactStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {

            statement.setString(1, status.name());
            statement.setLong(2, ownerId);
            statement.setLong(3, contactId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateCategory(long ownerId, long contactId, String category) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_CATEGORY_SQL)) {

            statement.setString(1, category);
            statement.setLong(2, ownerId);
            statement.setLong(3, contactId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteByIds(long ownerId, long contactId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, ownerId);
            statement.setLong(2, contactId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(long ownerId, long contactId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_SQL)) {

            statement.setLong(1, ownerId);
            statement.setLong(2, contactId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Pair<Long, Long>> getPendingRequestsByContactId(long contactId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_PENDING_BY_REQUEST_ID_SQL)) {
            stmt.setLong(1, contactId);
            List<Pair<Long, Long>> requests = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new Pair<>(rs.getLong("contact_id"), rs.getLong("owner_id")));
                }
            }
            return requests;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
