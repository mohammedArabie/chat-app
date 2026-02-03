package com.jets.chat.server.service;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.dto.UserDTO;

public interface UserService {

    public RegisterResponseDTO register(RegisterRequestDTO dto);
    public byte[] getProfilePicture(String picturePath);

    LoginResult login(String emailOrPhone, String password, ClientCallback callback);

    UserDTO reconnect(long userId, String sessionId, ClientCallback callback);

    void logout(long userId, String sessionId);
    public void clearOnlineUsers();
}
