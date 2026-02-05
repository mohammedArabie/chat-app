package com.jets.chat.server.dao.impl;

import com.jets.chat.server.dao.FileDao;
import com.jets.chat.server.entity.FileMetadata;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class FileDaoImpl implements FileDao {

    private final DataSource dataSource;

    public FileDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public FileMetadata save(FileMetadata file) {
        String sql = """
                INSERT INTO files (message_id, file_name, file_path, file_size, content_type)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, file.getMessageId());
            ps.setString(2, file.getFileName());
            ps.setString(3, file.getFilePath());
            ps.setLong(4, file.getFileSize());
            ps.setString(5, file.getContentType());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    file.setFileId(rs.getLong(1));
                    return file;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save file metadata", e);
        }
        return null;
    }

    @Override
    public Optional<FileMetadata> findById(long fileId) {
        String sql = "SELECT * FROM files WHERE file_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find file by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<FileMetadata> findByMessageId(long messageId) {
        String sql = "SELECT * FROM files WHERE message_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find file by message id", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteById(long fileId) {
        String sql = "DELETE FROM files WHERE file_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, fileId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    private FileMetadata mapRowToEntity(ResultSet rs) throws SQLException {
        FileMetadata file = new FileMetadata();
        file.setFileId(rs.getLong("file_id"));
        file.setMessageId(rs.getLong("message_id"));
        file.setFileName(rs.getString("file_name"));
        file.setFilePath(rs.getString("file_path"));
        file.setFileSize(rs.getLong("file_size"));
        file.setContentType(rs.getString("content_type"));
        return file;
    }
}