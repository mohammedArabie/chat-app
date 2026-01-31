package com.jets.chat.server.service.impl;

import com.jets.chat.server.dao.AdminDao;
import com.jets.chat.server.entity.Admin;
import com.jets.chat.server.service.AdminService;
import com.jets.chat.server.util.PasswordUtil;

import java.util.Optional;

public class AdminServiceImpl implements AdminService {
    private final AdminDao adminDao;

    public AdminServiceImpl(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    @Override
    public Optional<Admin> authenticate(String username, String password) {
        // HASH INPUT PASSWORD BEFORE COMPARISON
        String passwordHash = PasswordUtil.hash(password);

        Optional<Admin> adminOpt = adminDao.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            // COMPARE HASHES (NOT PLAIN TEXT)
            if (passwordHash.equals(admin.getPasswordHash())) {
                adminDao.updateLastLogin(admin.getAdminId());
                return Optional.of(admin);
            }
        }
        return Optional.empty();
    }


    @Override
    public boolean createAdmin(String username, String password) {
        //  HASH BEFORE STORAGE
        String passwordHash = PasswordUtil.hash(password);
        return adminDao.createAdmin(username, passwordHash);
    }

    @Override
    public boolean changePassword(Long adminId, String currentPassword, String newPassword) {
        // 1. Fetch admin by ID
        Optional<Admin> adminOpt = adminDao.findById(adminId);
        if (!adminOpt.isPresent()) {
            return false;
        }

        Admin admin = adminOpt.get();

        // 2. Verify current password matches stored hash
        String currentPasswordHash = PasswordUtil.hash(currentPassword);
        if (!currentPasswordHash.equals(admin.getPasswordHash())) {
            return false; // Current password incorrect
        }

        // 3. Update with new password hash
        String newPasswordHash = PasswordUtil.hash(newPassword);
        return adminDao.updatePassword(adminId, newPasswordHash);
    }

    @Override
    public String generateRandomPassword() {
        return adminDao.generateRandomPassword(12);
    }
}