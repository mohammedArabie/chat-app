package com.jets.chat.server.dao.impl;

import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.entity.Announcement;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AnnouncementDaoImpl implements AnnouncementDao {

    private final DataSource dataSource;

    public AnnouncementDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String INSERT_SQL = "INSERT INTO announcements (content, sent_at, font_style, font_color, is_bold, is_italic) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_RECENT_SQL = "SELECT * FROM announcements ORDER BY sent_at DESC LIMIT ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM announcements ORDER BY sent_at DESC";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM announcements WHERE announcement_id = ?";
    private static final String UPDATE_SQL = "UPDATE announcements SET content = ?, font_style = ?, font_color = ?, is_bold = ?, is_italic = ? WHERE announcement_id = ?";
    private static final String DELETE_SQL = "DELETE FROM announcements WHERE announcement_id = ?";

    @Override
    public Announcement save(Announcement announcement) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, announcement.getContent());
            if (announcement.getSentAt() == null) {
                announcement.setSentAt(new Timestamp(System.currentTimeMillis()));
            }
            statement.setTimestamp(2, announcement.getSentAt());
            statement.setString(3, announcement.getFontStyle());
            statement.setString(4, announcement.getFontColor());
            statement.setBoolean(5, announcement.isBold());
            statement.setBoolean(6, announcement.isItalic());

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Couldn't save to the database: NO ROWS AFFECTED");
            }

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    announcement.setAnnouncementId(generatedId);
                }
            }

            return announcement;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Announcement> findRecent(int limit) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_RECENT_SQL)) {
            statement.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = statement.executeQuery()) {
                List<Announcement> announcements = new ArrayList<>();
                while (rs.next()) {
                    announcements.add(mapRowToAnnouncement(rs));
                }
                return announcements;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Announcement> findAll() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL)) {
            try (ResultSet rs = statement.executeQuery()) {
                List<Announcement> announcements = new ArrayList<>();
                while (rs.next()) {
                    announcements.add(mapRowToAnnouncement(rs));
                }
                return announcements;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Announcement> findById(long id) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToAnnouncement(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Announcement announcement) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, announcement.getContent());
            statement.setString(2, announcement.getFontStyle());
            statement.setString(3, announcement.getFontColor());
            statement.setBoolean(4, announcement.isBold());
            statement.setBoolean(5, announcement.isItalic());
            statement.setLong(6, announcement.getAnnouncementId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Announcement mapRowToAnnouncement(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(rs.getLong("announcement_id"));
        announcement.setContent(rs.getString("content"));
        announcement.setSentAt(rs.getTimestamp("sent_at"));
        announcement.setFontStyle(rs.getString("font_style"));
        announcement.setFontColor(rs.getString("font_color"));
        announcement.setBold(rs.getBoolean("is_bold"));
        announcement.setItalic(rs.getBoolean("is_italic"));
        return announcement;
    }
}
