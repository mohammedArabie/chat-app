package com.jets.chat.server.service.impl;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.dto.UserDTO;
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
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {

        this.userDao = userDao;
    }

    private static final String UPLOAD_BASE_DIR;

    static {
        String userHome = System.getProperty("user.home");
        UPLOAD_BASE_DIR = userHome + File.separator + ProjectConstants.UPLOAD_FOLDER_NAME
                + File.separator + ProjectConstants.PROFILES_SUBFOLDER + File.separator;
        System.out.println("Profile uploads directory: " + UPLOAD_BASE_DIR);

        initializeUploadDirectory();
    }

    private static void initializeUploadDirectory() {
        File directory = new File(UPLOAD_BASE_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created upload directory: " + UPLOAD_BASE_DIR);
            } else {
                System.err.println("Failed to create upload directory: " + UPLOAD_BASE_DIR);
            }
        } else {
            System.out.println("Upload directory exists: " + UPLOAD_BASE_DIR);
        }
    }

    private boolean isValidImage(String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            if (imageBytes.length > 5 * 1024 * 1024) {
                System.err.println("Image too large: " + imageBytes.length + " bytes");
                return false;
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                System.err.println("Not a valid image file");
                return false;
            }

            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 encoding");
            return false;
        } catch (IOException e) {
            System.err.println("Failed to read image data");
            return false;
        }
    }

    private String detectImageFormat(byte[] imageBytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image != null && imageBytes.length > 4) {
                // PNG signature: 89 50 4E 47
                if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == 0x50 && imageBytes[2] == 0x4E
                        && imageBytes[3] == 0x47) {
                    return "png";
                }
                // GIF signature: 47 49 46 38
                if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46
                        && imageBytes[3] == 0x38) {
                    return "gif";
                }
                // JPEG signature: FF D8 FF
                if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8
                        && imageBytes[2] == (byte) 0xFF) {
                    return "jpg";
                }
            }
        } catch (IOException e) {
            // Ignore
        }

        return "jpg"; // Default
    }

    private String saveProfilePicture(String base64Image, String userEmail) {
        try {
            if (!isValidImage(base64Image)) {
                System.err.println("Invalid image file for user: " + userEmail);
                return null;
            }

            // Decode Base64 to bytes
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Detect image format
            String extension = detectImageFormat(imageBytes);

            // Generate unique filename with UUID to prevent collisions
            String sanitizedEmail = userEmail.replace("@", "_").replace(".", "_")
                    .replaceAll("[^a-zA-Z0-9_]", "");
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String fileName = sanitizedEmail + "_" + uuid + "_" + System.currentTimeMillis() + "."
                    + extension;

            // FIXED: Use UPLOAD_BASE_DIR instead of UPLOAD_DIR
            String filePath = UPLOAD_BASE_DIR + fileName;

            // Write file to disk
            Files.write(Paths.get(filePath), imageBytes);

            System.out.println("✓ Profile picture saved: " + filePath);
            return filePath;

        } catch (IllegalArgumentException e) {
            System.err.println("✗ Invalid Base64 image data");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("✗ Failed to write profile picture to disk");
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getProfilePicture(String picturePath) {
        try {
            File file = new File(picturePath);
            if (file.exists()) {
                return Files.readAllBytes(file.toPath());
            } else {
                System.err.println("Profile picture not found: " + picturePath);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Failed to read profile picture: " + picturePath);
            e.printStackTrace();
            return null;
        }
    }

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

        String hashedPassword = PasswordUtil.hash(dto.getPassword());
        user.setPasswordHash(hashedPassword);

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            String picturePath = saveProfilePicture(dto.getProfilePicture(), dto.getEmail());
            if (picturePath != null) {
                user.setPicturePath(picturePath);
            } else {
                System.err.println("Failed to save profile picture for user: " + dto.getEmail());
            }
        }

        User savedUser = userDao.save(user);

        userDao.updateStatus(savedUser.getUserId(), UserStatus.OFFLINE);

        return new RegisterResponseDTO(true, "Registration successful", savedUser.getUserId());
    }

    @Override
    public LoginResult login(String input, String password, ClientCallback callback) {
        Optional<User> userOpt;
        if (input.contains("@")) {
            userOpt = userDao.findByEmail(input);
        } else {
            userOpt = userDao.findByPhoneNumber(input);
        }

        if (userOpt.isEmpty()) {
            return new LoginResult("User not found");
        }

        User user = userOpt.get();

        String incomingHash = PasswordUtil.hash(password);
        if (!user.getPasswordHash().equals(incomingHash)) {
            return new LoginResult("Invalid credentials");
        }

        String sessionId = UUID.randomUUID().toString();
        boolean sessionCreated = userDao.createSession(sessionId, user.getUserId());

        if (!sessionCreated) {
            return new LoginResult("Server error: Could not create session");
        }

        userDao.updateStatus(user.getUserId(), UserStatus.AVAILABLE);
        ServerManager.getInstance().getOnlineClients().put(user.getUserId(), callback);

        System.out.println("User logged in: " + user.getDisplayName());

        UserDTO dto = DtoMapper.toUserDTO(user);

        return new LoginResult(dto, sessionId);
    }

    @Override
    public UserDTO reconnect(long userId, String sessionId, ClientCallback callback) {
        if (!userDao.isSessionValid(userId, sessionId)) {
            System.out.println("Reconnection failed: Invalid session for user " + userId);
            return null;
        }

        Optional<User> userOpt = userDao.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            ServerManager.getInstance().getOnlineClients().put(userId, callback);

            userDao.updateStatus(userId, UserStatus.AVAILABLE);

            System.out.println("User reconnected: " + user.getDisplayName());

            UserDTO dto = DtoMapper.toUserDTO(user);
            dto.setStatus(UserStatus.AVAILABLE);
            return dto;
        }

        return null;
    }

    @Override
    public void logout(long userId, String sessionId) {
        ServerManager.getInstance().getOnlineClients().remove(userId);
        userDao.deleteSession(sessionId);
        userDao.updateStatus(userId, UserStatus.OFFLINE);
    }
}
