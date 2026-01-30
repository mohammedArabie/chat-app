package com.jets.chat.server.service.impl;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.*;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.util.ProjectConstants;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.entity.User;
import com.jets.chat.server.service.UserService;
import com.jets.chat.server.util.DtoMapper;
import com.jets.chat.server.util.PasswordUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UserServiceImpl implements UserService {

    private static final String UPLOAD_BASE_DIR;
    private final UserDao userDao;

    static {
        String userHome = System.getProperty("user.home");
        UPLOAD_BASE_DIR = userHome + File.separator + ProjectConstants.UPLOAD_FOLDER_NAME
                + File.separator + ProjectConstants.PROFILES_SUBFOLDER + File.separator;
        initializeUploadDirectory();
    }

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    // --- Authentication & Session Methods ---

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO dto) {
        if (userDao.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            return new RegisterResponseDTO(false, "Phone number already registered", null);
        }
        User user = new User();
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDisplayName(dto.getDisplayName());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setCountry(dto.getCountry());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setBio(dto.getBio());
        user.setPasswordHash(PasswordUtil.hash(dto.getPassword()));

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            String picturePath = saveProfilePicture(dto.getProfilePicture(), dto.getEmail());
            if (picturePath != null) user.setPicturePath(picturePath);
        }

        User savedUser = userDao.save(user);
        userDao.updateStatus(savedUser.getUserId(), UserStatus.OFFLINE);
        return new RegisterResponseDTO(true, "Registration successful", savedUser.getUserId());
    }

    @Override
    public LoginResult login(String input, String password, ClientCallback callback) {
        Optional<User> userOpt = input.contains("@") ? userDao.findByEmail(input) : userDao.findByPhoneNumber(input);
        if (userOpt.isEmpty()) return new LoginResult("User not found");

        User user = userOpt.get();
        if (!user.getPasswordHash().equals(PasswordUtil.hash(password))) {
            return new LoginResult("Invalid credentials");
        }

        String sessionId = UUID.randomUUID().toString();
        if (!userDao.createSession(sessionId, user.getUserId())) {
            return new LoginResult("Server error: Could not create session");
        }

        userDao.updateStatus(user.getUserId(), UserStatus.AVAILABLE);
        ServerManager.getInstance().getOnlineClients().put(user.getUserId(), callback);
        return new LoginResult(convertToDto(user), sessionId);
    }

    @Override
    public UserDTO reconnect(long userId, String sessionId, ClientCallback callback) {
        if (!userDao.isSessionValid(userId, sessionId)) return null;
        return userDao.findById(userId).map(user -> {
            ServerManager.getInstance().getOnlineClients().put(userId, callback);
            userDao.updateStatus(userId, UserStatus.AVAILABLE);
            UserDTO dto = convertToDto(user);
            dto.setStatus(UserStatus.AVAILABLE);
            return dto;
        }).orElse(null);
    }

    @Override
    public void logout(long userId, String sessionId) {
        ServerManager.getInstance().getOnlineClients().remove(userId);
        userDao.deleteSession(sessionId);
        userDao.updateStatus(userId, UserStatus.OFFLINE);
    }

    // --- User Management Methods ---

    @Override
    public Optional<UserDTO> findUserById(long id) {
        return userDao.findById(id).map(this::convertToDto);
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userDao.findByEmail(email).map(this::convertToDto);
    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        return userDao.update(convertToEntity(userDTO));
    }

    @Override
    public boolean updateUserPassword(long id, String passwordHash) {
        return userDao.updatePassword(id, passwordHash);
    }

    @Override
    public boolean phoneNumberExists(String phoneNumber) {
        return phoneNumber != null && userDao.findByPhoneNumber(phoneNumber.trim()).isPresent();
    }

    // --- Status Management ---

    @Override
    public boolean updateUserStatus(long userId, UserStatus status) {
        return userDao.updateStatus(userId, status);
    }

    @Override
    public UserStatus getUserStatus(long userId) {
        return userDao.getStatus(userId);
    }

    @Override
    public void clearOnlineUsers() {
        ServerManager.getInstance().getOnlineClients().clear();
    }

    // --- Image Handling & Helpers ---

    @Override
    public byte[] getProfilePicture(String picturePath) {
        try {
            File file = new File(picturePath);
            return file.exists() ? Files.readAllBytes(file.toPath()) : null;
        } catch (IOException e) { return null; }
    }

    private String saveProfilePicture(String base64Image, String userEmail) {
        try {
            if (!isValidImage(base64Image)) return null;
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            String extension = detectImageFormat(imageBytes);
            String fileName = userEmail.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString().substring(0,8) + "." + extension;
            String filePath = UPLOAD_BASE_DIR + fileName;
            Files.write(Paths.get(filePath), imageBytes);
            return filePath;
        } catch (Exception e) { return null; }
    }

    private boolean isValidImage(String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            if (imageBytes.length > 5 * 1024 * 1024) return false;
            return ImageIO.read(new ByteArrayInputStream(imageBytes)) != null;
        } catch (Exception e) { return false; }
    }

    private String detectImageFormat(byte[] imageBytes) {
        if (imageBytes.length < 4) return "jpg";
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == 0x50) return "png";
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) return "jpg";
        if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49) return "gif";
        return "jpg";
    }

    private static void initializeUploadDirectory() {
        File directory = new File(UPLOAD_BASE_DIR);
        if (!directory.exists()) directory.mkdirs();
    }

    // --- Mappers ---

    private UserDTO convertToDto(User entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getUserId());
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
        dto.setStatus(userDao.getStatus(entity.getUserId()));
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User entity = new User();
        entity.setUserId(dto.getId());
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