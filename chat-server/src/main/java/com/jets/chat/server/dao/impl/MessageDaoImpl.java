package com.jets.chat.server.dao.impl;

import com.jets.chat.server.dao.MessageDao;
import com.jets.chat.server.entity.Message;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDaoImpl implements MessageDao {

    private final DataSource dataSource;

    public MessageDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Message save(Message message) {
        String sql = """
                INSERT INTO messages
                (chat_id, sender_id, message_type, content, font_style, font_color,
                 font_size, is_bold, is_italic, is_underline, background_color)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, message.getChatId());
            ps.setLong(2, message.getSenderId());
            ps.setString(3, message.getMessageType());
            ps.setString(4, message.getContent());
            ps.setString(5, message.getFontStyle());
            ps.setString(6, message.getFontColor());
            ps.setInt(7, message.getFontSize());
            ps.setBoolean(8, message.isBold());
            ps.setBoolean(9, message.isItalic());
            ps.setBoolean(10, message.isUnderline());
            ps.setString(11, message.getBackgroundColor());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return findById(id).orElseThrow(
                            () -> new RuntimeException("Save succeeded but fetch failed"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save message", e);
        }
        return null;
    }

    @Override
    public Optional<Message> findById(long id) {
        String sql = "SELECT * FROM messages WHERE message_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find message by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> findByChatId(long chatId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages where chat_id = ? ORDER BY sent_at ASC";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all messages", e);
        }
        return messages;
    }

    @Override
    public Message update(Message message) {
        String sql = """
                UPDATE messages SET
                    content = ?,
                    font_style = ?,
                    font_color = ?,
                    font_size = ?,
                    is_bold = ?,
                    is_italic = ?,
                    is_underline = ?,
                    background_color = ?
                WHERE message_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, message.getContent());
            ps.setString(2, message.getFontStyle());
            ps.setString(3, message.getFontColor());
            ps.setInt(4, message.getFontSize());
            ps.setBoolean(5, message.isBold());
            ps.setBoolean(6, message.isItalic());
            ps.setBoolean(7, message.isUnderline());
            ps.setString(8, message.getBackgroundColor());
            ps.setLong(9, message.getMessageId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                return message;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update message", e);
        }
        return null;
    }

    @Override
    public Optional<Message> deleteById(long id) {
        Optional<Message> existing = findById(id);

        String sql = "DELETE FROM messages WHERE message_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();

            return (rowsAffected > 0) ? existing : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete message", e);
        }
    }

    public Optional<Message> findLatest(long chatId) {
        String sql = "SELECT * FROM messages WHERE chat_id = ? ORDER BY sent_at DESC LIMIT 1";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Message mapRowToEntity(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setMessageId(rs.getLong("message_id"));
        m.setChatId(rs.getLong("chat_id"));
        m.setSenderId(rs.getLong("sender_id"));
        m.setMessageType(rs.getString("message_type"));
        m.setContent(rs.getString("content"));
        m.setFontStyle(rs.getString("font_style"));
        m.setFontColor(rs.getString("font_color"));
        m.setFontSize(rs.getInt("font_size"));
        m.setBold(rs.getBoolean("is_bold"));
        m.setItalic(rs.getBoolean("is_italic"));
        m.setUnderline(rs.getBoolean("is_underline"));
        m.setBackgroundColor(rs.getString("background_color"));
        m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
        return m;
    }
}
