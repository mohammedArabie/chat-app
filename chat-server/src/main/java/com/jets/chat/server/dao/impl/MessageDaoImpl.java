package com.jets.chat.server.dao.impl;

import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.MessageDao;
import com.jets.chat.server.dto.CreateMessageDto;
import com.jets.chat.server.dto.EditMessageDto;
import com.jets.chat.server.dto.MessageResponseDto;

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
    public MessageResponseDto save(CreateMessageDto dto) {
        String sql = "INSERT INTO messages (chat_id, sender_id, message_type, content, font_style, font_color, font_size, is_bold, is_italic, is_underline, background_color) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, dto.getChatId());
            ps.setLong(2, dto.getSenderId());
            ps.setString(3, dto.getMessageType());
            ps.setString(4, dto.getContent());
            ps.setString(5, dto.getFontStyle());
            ps.setString(6, dto.getFontColor());
            ps.setInt(7, dto.getFontSize());
            ps.setBoolean(8, dto.isBold());
            ps.setBoolean(9, dto.isItalic());
            ps.setBoolean(10, dto.isUnderline());
            ps.setString(11, dto.getBackgroundColor());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return findById(id).orElse(null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MessageResponseDto> findAll() {
        List<MessageResponseDto> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.display_name FROM messages m " +
                "JOIN users u ON m.sender_id = u.user_id ORDER BY m.sent_at ASC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                messages.add(mapResultSetToResponseDto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Optional<MessageResponseDto> findById(long id) {
        String sql = "SELECT m.*, u.display_name FROM messages m " +
                "JOIN users u ON m.sender_id = u.user_id WHERE m.message_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResponseDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public MessageResponseDto update(EditMessageDto dto) {
        String sql = "UPDATE messages SET content = ?, font_style = ?, font_color = ?, " +
                "font_size = ?, is_bold = ?, is_italic = ?, is_underline = ?, background_color = ? " +
                "WHERE message_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getContent());
            ps.setString(2, dto.getFontStyle());
            ps.setString(3, dto.getFontColor());
            ps.setInt(4, dto.getFontSize());
            ps.setBoolean(5, dto.isBold());
            ps.setBoolean(6, dto.isItalic());
            ps.setBoolean(7, dto.isUnderline());
            ps.setString(8, dto.getBackgroundColor());
            ps.setLong(9, dto.getMessageId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                return findById(dto.getMessageId()).orElse(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MessageResponseDto deleteById(long id) {
        Optional<MessageResponseDto> toDelete = findById(id);
        if (toDelete.isPresent()) {
            String sql = "DELETE FROM messages WHERE message_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.executeUpdate();
                return toDelete.get();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private MessageResponseDto mapResultSetToResponseDto(ResultSet rs) throws SQLException {
        MessageResponseDto dto = new MessageResponseDto();
        dto.setMessageId(rs.getLong("message_id"));
        dto.setSenderName(rs.getString("display_name"));
        dto.setContent(rs.getString("content"));
        dto.setMessageType(rs.getString("message_type"));
        dto.setSentAtFromTimestamp(rs.getTimestamp("sent_at"));
        dto.setFontStyle(rs.getString("font_style"));
        dto.setFontColor(rs.getString("font_color"));
        dto.setFontSize(rs.getInt("font_size"));
        dto.setBold(rs.getBoolean("is_bold"));
        dto.setItalic(rs.getBoolean("is_italic"));
        dto.setUnderline(rs.getBoolean("is_underline"));
        dto.setBackgroundColor(rs.getString("background_color"));
        return dto;
    }

    private MessageDaoImpl instance;

    public MessageDaoImpl getInstance() {
        if (instance == null) {
            synchronized (MessageDaoImpl.class) {
                if (instance == null) {
                    instance = new MessageDaoImpl(DataSourceConfig.getDataSource());
                }
            }
        }
        return instance;
    }
}