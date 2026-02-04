package com.jets.chat.server.dao.impl;

import com.jets.chat.common.enums.Gender;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.entity.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final String INSERT_USER_SQL = "INSERT INTO users "
            + "(phone_number, display_name, email, password_hash, gender, country, date_of_birth, bio, picture_path, chatbot_enabled) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_STATUS_SQL = "INSERT INTO user_status (user_id, status) VALUES (?, 'OFFLINE')";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_BY_PHONE_SQL = "SELECT * FROM users WHERE phone_number = ?";
    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM users WHERE email = ?";
    private static final String UPDATE_USER_SQL = "UPDATE users SET display_name = ?, email = ?, gender = ?, country = ?, date_of_birth = ?, bio = ?, picture_path = ?, chatbot_enabled = ? "
            + "WHERE user_id = ?";
    private static final String UPDATE_PASSWORD_SQL = "UPDATE users SET password_hash = ? WHERE user_id = ?";
    private static final String UPDATE_STATUS_SQL = "UPDATE user_status SET status = ?, last_seen = CURRENT_TIMESTAMP WHERE user_id = ?";
    private static final String GET_STATUS_SQL = "SELECT status FROM user_status WHERE user_id = ?";
    private static final String INSERT_SESSION_SQL = "INSERT INTO user_sessions (session_id, user_id) VALUES (?, ?)";
    private static final String DELETE_SESSION_SQL = "DELETE FROM user_sessions WHERE session_id = ?";
    private static final String VALIDATE_SESSION_SQL = "SELECT 1 FROM user_sessions WHERE user_id = ? AND session_id = ?";
    private static final String GET_PENDING_REQUESTS_SQL = "SELECT * FROM contacts WHERE status = 'PENDING' AND contact_id = ?";
    private static final String INSERT_REQUEST_SQL = "INSERT INTO contacts (owner_id, contact_id) VALUES(?, ?)";
    private final DataSource dataSource;

    public UserDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_USER_SQL,
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getPhoneNumber());
                stmt.setString(2, user.getDisplayName());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getPasswordHash());
                stmt.setString(5, user.getGender().name());
                stmt.setString(6, user.getCountry());
                if (user.getDateOfBirth() != null) {
                    stmt.setDate(7, new java.sql.Date(user.getDateOfBirth().getTime()));
                } else {
                    stmt.setNull(7, java.sql.Types.DATE);
                }
                stmt.setString(8, user.getBio());
                stmt.setString(9, user.getPicturePath());
                stmt.setBoolean(10, user.isChatbotEnabled());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_STATUS_SQL)) {
                stmt.setLong(1, user.getUserId());
                stmt.executeUpdate();
            }

            conn.commit();
            return user;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Optional<User> findById(long id) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(FIND_BY_PHONE_SQL)) {

            stmt.setString(1, phoneNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(FIND_BY_EMAIL_SQL)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean update(User user) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_SQL)) {

            stmt.setString(1, user.getDisplayName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getGender().name());
            stmt.setString(4, user.getCountry());
            if (user.getDateOfBirth() != null) {
                stmt.setDate(5, new java.sql.Date(user.getDateOfBirth().getTime()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            stmt.setString(6, user.getBio());
            stmt.setString(7, user.getPicturePath());
            stmt.setBoolean(8, user.isChatbotEnabled());
            stmt.setLong(9, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updatePassword(long id, String passwordHash) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_PASSWORD_SQL)) {

            stmt.setString(1, passwordHash);
            stmt.setLong(2, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStatus(long userId, UserStatus status) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS_SQL)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserStatus getStatus(long userId) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(GET_STATUS_SQL)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UserStatus.valueOf(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return UserStatus.OFFLINE;
    }

    @Override
    public boolean createSession(String sessionId, long userId) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_SESSION_SQL)) {

            stmt.setString(1, sessionId);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteSession(String sessionId) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_SESSION_SQL)) {

            stmt.setString(1, sessionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // UserDaoImpl.java
    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setDisplayName(rs.getString("display_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash")); // Not used in UI
                user.setGender(Gender.valueOf(rs.getString("gender")));
                user.setCountry(rs.getString("country"));
                user.setDateOfBirth(rs.getDate("date_of_birth"));
                user.setBio(rs.getString("bio"));
                user.setPicturePath(rs.getString("picture_path"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setChatbotEnabled(rs.getBoolean("chatbot_enabled"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch users", e);
        }

        return users;
    }

    @Override
    public boolean isSessionValid(long userId, String sessionId) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(VALIDATE_SESSION_SQL)) {
            stmt.setLong(1, userId);
            stmt.setString(2, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setDisplayName(rs.getString("display_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setGender(Gender.valueOf(rs.getString("gender")));
        user.setCountry(rs.getString("country"));
        user.setDateOfBirth(rs.getDate("date_of_birth"));
        user.setBio(rs.getString("bio"));
        user.setPicturePath(rs.getString("picture_path"));
        user.setChatbotEnabled(rs.getBoolean("chatbot_enabled"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}