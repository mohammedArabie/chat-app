package com.jets.chat.server.dao.impl;

import com.jets.chat.server.dao.AdminDao;
import com.jets.chat.server.entity.Admin;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.Optional;
import java.util.Random;

public class AdminDaoImpl implements AdminDao {
    private final HikariDataSource dataSource;

    public AdminDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setAdminId(rs.getLong("admin_id"));
                admin.setUsername(rs.getString("username"));
                admin.setPasswordHash(rs.getString("password_hash")); // HASHED
                admin.setCreatedAt(rs.getTimestamp("created_at"));
                admin.setLastLogin(rs.getTimestamp("last_login"));
                admin.setMustChangePassword(rs.getBoolean("must_change_password"));
                return Optional.of(admin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    @Override
    public Optional<Admin> findById(Long adminId) {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, adminId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Admin admin = new Admin();
                admin.setAdminId(rs.getLong("admin_id"));
                admin.setUsername(rs.getString("username"));
                admin.setPasswordHash(rs.getString("password_hash"));
                admin.setCreatedAt(rs.getTimestamp("created_at"));
                admin.setLastLogin(rs.getTimestamp("last_login"));
                admin.setMustChangePassword(rs.getBoolean("must_change_password"));
                return Optional.of(admin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean createAdmin(String username, String passwordHash) { // HASHED
        String sql = "INSERT INTO admins (username, password_hash, must_change_password) VALUES (?, ?, TRUE)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash); // STORE HASH (NOT PLAIN TEXT)
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updatePassword(Long adminId, String newPasswordHash) { // HASHED
        String sql = "UPDATE admins SET password_hash = ? WHERE admin_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash); // STORE HASH
            stmt.setLong(2, adminId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateLastLogin(Long adminId) {
        String sql = "UPDATE admins SET last_login = CURRENT_TIMESTAMP WHERE admin_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, adminId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    // ADD THIS METHOD to AdminDaoImpl:
    @Override
    public boolean updateMustChangePassword(Long adminId, boolean mustChangePassword) {
        String sql = "UPDATE admins SET must_change_password = ? WHERE admin_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, mustChangePassword);
            stmt.setLong(2, adminId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}