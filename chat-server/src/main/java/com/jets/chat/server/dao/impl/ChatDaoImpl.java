package com.jets.chat.server.dao.impl;

import com.jets.chat.server.dao.ChatDao;
import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatGroup;
import com.jets.chat.server.entity.ChatParticipant;
import com.jets.chat.server.entity.ChatType;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ChatDaoImpl implements ChatDao {

    private final DataSource dataSource;

    // ================== CONSTRUCTOR ==================
    public ChatDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ================== CHAT CREATION ==================
    @Override
    public long insertChat(ChatType type) {
        String sql = "INSERT INTO chats (chat_type) VALUES (?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, type.name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to generate chat ID");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert chat", e);
        }
    }


    @Override
    public void insertGroup(long chatId, String groupName, long ownerId) {
        String sql = "INSERT INTO chat_groups (chat_id, group_name, owner_id) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setString(2, groupName);
            ps.setLong(3, ownerId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert chat group", e);
        }
    }


    // ================== CHAT RETRIEVAL ==================
    @Override
    public Optional<Chat> findById(long chatId) {
        String sql = "SELECT chat_id, chat_type, created_at FROM chats WHERE chat_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(
                        new Chat(rs.getLong("chat_id"), ChatType.valueOf(rs.getString("chat_type")),
                                rs.getTimestamp("created_at")));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find chat by ID: " + chatId, e);
        }
    }

    @Override
    public List<Chat> findChatsByUser(long userId) {
        String sql = """
                SELECT c.chat_id, c.chat_type, c.created_at
                FROM chats c
                JOIN chat_participants p ON c.chat_id = p.chat_id
                WHERE p.user_id = ?
                """;

        List<Chat> chats = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                chats.add(
                        new Chat(rs.getLong("chat_id"), ChatType.valueOf(rs.getString("chat_type")),
                                rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find chats for user: " + userId, e);
        }

        return chats;
    }

    // ================== PARTICIPANTS ==================
    @Override
    public boolean addParticipant(long chatId, long userId) {
        try (Connection connection = dataSource.getConnection()) {
            return addParticipant(connection, chatId, userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add participant", e);
        }
    }

    private boolean addParticipant(Connection connection, long chatId, long userId)
            throws SQLException {
        String sql = "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setLong(2, userId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    @Override
    public boolean removeParticipant(long chatId, long userId) {
        String sql = "DELETE FROM chat_participants WHERE chat_id = ? AND user_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setLong(2, userId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove participant", e);
        }
    }

    @Override
    public List<ChatParticipant> getParticipants(long chatId) {
        String sql = "SELECT chat_id, user_id, joined_at FROM chat_participants WHERE chat_id = ?";

        List<ChatParticipant> participants = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                participants.add(new ChatParticipant(rs.getLong("chat_id"), rs.getLong("user_id"),
                        rs.getTimestamp("joined_at")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get participants for chat: " + chatId, e);
        }

        return participants;
    }

    // ================== GROUP OPERATIONS ==================
    @Override
    public Optional<ChatGroup> getGroupInfo(long chatId) {
        String sql = "SELECT chat_id, group_name, owner_id FROM chat_groups WHERE chat_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(new ChatGroup(rs.getLong("chat_id"), rs.getString("group_name"),
                        rs.getLong("owner_id")));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get group info for chat: " + chatId, e);
        }
    }

    @Override
    public boolean renameGroup(long chatId, String newName) {
        String sql = "UPDATE chat_groups SET group_name = ? WHERE chat_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setLong(2, chatId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to rename group", e);
        }
    }

    @Override
    public boolean changeGroupOwner(long chatId, long newOwnerId) {
        String sql = "UPDATE chat_groups SET owner_id = ? WHERE chat_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, newOwnerId);
            ps.setLong(2, chatId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to change group owner", e);
        }
    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void restoreAutoCommit(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}