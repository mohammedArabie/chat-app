package com.jets.chat.server.service;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;

import java.util.Optional;

public interface UserService {

    RegisterResponseDTO register(RegisterRequestDTO dto);

    LoginResult login(String emailOrPhone, String password, ClientCallback callback);

    UserDTO reconnect(long userId, String sessionId, ClientCallback callback);

    void logout(long userId, String sessionId);

    void clearOnlineUsers();

    boolean phoneNumberExists(String phoneNumber);


    Optional<UserDTO> findUserById(long id);

    Optional<UserDTO> getUserByEmail(String email);

    byte[] getProfilePicture(String picturePath);

    boolean updateUser(UserDTO userDTO);

    boolean updateUserPassword(long id, String passwordHash);


    boolean updateUserStatus(long userId, UserStatus status);

    UserStatus getUserStatus(long userId);
}