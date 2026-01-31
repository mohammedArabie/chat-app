package com.jets.chat.server.dao;

import com.jets.chat.server.entity.Admin;
import java.util.Optional;

public interface AdminDao {
    Optional<Admin> findByUsername(String username);
    boolean createAdmin(String username, String passwordHash);  // STORE HASH
    boolean updatePassword(Long adminId, String newPasswordHash);  // STORE HASH
    boolean updateLastLogin(Long adminId);
    String generateRandomPassword(int length);
}