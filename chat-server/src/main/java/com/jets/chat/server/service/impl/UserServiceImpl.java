package com.jets.chat.server.service.impl;

import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.entity.User;
import com.jets.chat.server.service.UserService;

import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<UserDTO> findUserById(long id) {
        return userDao.findById(id).map(this::convertToDto);
    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        return userDao.update(user);
    }

    @Override
    public boolean updateUserPassword(long id, String passwordHash) {
        return userDao.updatePassword(id, passwordHash);
    }

    @Override
    public boolean updateUserStatus(long userId, UserStatus status) {
        return userDao.updateStatus(userId, status);
    }

    @Override
    public UserStatus getUserStatus(long userId) {
        return userDao.getStatus(userId);
    }

    private UserDTO convertToDto(User entity) {
        UserDTO dto = new UserDTO();
        dto.setUserId(entity.getUserId());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setDisplayName(entity.getDisplayName());
        dto.setEmail(entity.getEmail());
        dto.setPasswordHash(entity.getPasswordHash());
        dto.setGender(entity.getGender());
        dto.setCountry(entity.getCountry());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setBio(entity.getBio());
        dto.setPicturePath(entity.getPicturePath());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setChatbotEnabled(entity.isChatbotEnabled());
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User entity = new User();
        entity.setUserId(dto.getUserId());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setDisplayName(dto.getDisplayName());
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(dto.getPasswordHash());
        entity.setGender(dto.getGender());
        entity.setCountry(dto.getCountry());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setBio(dto.getBio());
        entity.setPicturePath(dto.getPicturePath());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setChatbotEnabled(dto.isChatbotEnabled());
        return entity;
    }
}