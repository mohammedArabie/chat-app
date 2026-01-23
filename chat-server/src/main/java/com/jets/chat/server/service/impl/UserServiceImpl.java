package com.jets.chat.server.service.impl;

import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.entity.User;
import com.jets.chat.server.service.UserService;
import com.jets.chat.server.util.PasswordUtil;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
    public RegisterResponseDTO register(RegisterRequestDTO dto) {

        // 1️⃣ Check phone uniqueness
        if (userDao.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            return new RegisterResponseDTO(false, "Phone number already registered", null);
        }

        // 2️⃣ Create User entity
        User user = new User();
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDisplayName(dto.getDisplayName());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setCountry(dto.getCountry());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setBio(dto.getBio());

        // 3️⃣ Hash password
        String hashedPassword = PasswordUtil.hash(dto.getPassword());
        user.setPasswordHash(hashedPassword);

        // 4️⃣ Save user
        User savedUser = userDao.save(user);

        // 5️⃣ Initialize status
        userDao.updateStatus(savedUser.getUserId(), UserStatus.OFFLINE);

        return new RegisterResponseDTO(true, "Registration successful", savedUser.getUserId());
    }
}
