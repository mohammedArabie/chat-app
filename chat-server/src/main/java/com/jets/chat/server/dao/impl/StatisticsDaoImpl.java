package com.jets.chat.server.dao.impl;

import com.jets.chat.server.dao.StatisticsDao;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StatisticsDaoImpl implements StatisticsDao {
    private final HikariDataSource dataSource;

    public StatisticsDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int getOnlineUsersCount() {
        String sql = "SELECT COUNT(*) FROM user_status WHERE status != 'OFFLINE'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getOfflineUsersCount() {
        String sql = "SELECT COUNT(*) FROM user_status WHERE status = 'OFFLINE'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Map<String, Long> getUserGenderStatistics() {
        Map<String, Long> stats = new HashMap<>();
        String sql = "SELECT gender, COUNT(*) as count FROM users WHERE gender IS NOT NULL GROUP BY gender";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("gender"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    @Override
    public Map<String, Long> getUserCountryStatistics() {
        Map<String, Long> stats = new HashMap<>();
        String sql = "SELECT country, COUNT(*) as count FROM users WHERE country IS NOT NULL AND country != '' GROUP BY country ORDER BY count DESC LIMIT 10";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("country"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    @Override
    public long getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long getTotalMessages() {
        String sql = "SELECT COUNT(*) FROM messages";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long getTotalChats() {
        String sql = "SELECT COUNT(*) FROM chats";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}