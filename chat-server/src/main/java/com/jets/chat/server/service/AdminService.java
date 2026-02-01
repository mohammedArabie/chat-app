package com.jets.chat.server.service;

import com.jets.chat.server.entity.Admin;
import java.util.Optional;

public interface AdminService {
    Optional<Admin> authenticate(String username, String password); // HASHES INPUT BEFORE
                                                                    // COMPARISON
    boolean createAdmin(String username, String password); // HASHES BEFORE STORAGE
    boolean changePassword(Long adminId, String currentPassword, String newPassword); // HASHES NEW
                                                                                      // PASSWORD
    String generateRandomPassword();

}