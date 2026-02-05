package com.jets.chat.server.service;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserService {

    public RegisterResponseDTO register(RegisterRequestDTO dto);

    public byte[] getProfilePicture(String picturePath);

    LoginResult login(String emailOrPhone, String password, ClientCallback callback);

    UserDTO reconnect(long userId, String sessionId, ClientCallback callback);

    void logout(long userId, String sessionId);

    public void clearOnlineUsers();
    boolean phoneNumberExists(String phoneNumber);

    public Optional<UserDTO> getUserByEmail(String email);

    Optional<UserDTO> getUserById(long userId);

    void updateStatus(long userId, UserStatus status);

    UserStatus getUserStatus(long userId);

    List<Long> getUserContacts(long userId);

    void notifyContactsOfStatusChange(long userId, UserStatus status);

    UserDTO updateUserProfile(long userId, String displayName, String email);

    boolean isEmailExists(String email);
}
