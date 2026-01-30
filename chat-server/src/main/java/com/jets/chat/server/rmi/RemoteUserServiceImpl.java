package com.jets.chat.server.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.*;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.server.service.UserService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;

public class RemoteUserServiceImpl extends UnicastRemoteObject implements RemoteUserService {

    private final UserService userService;

    public RemoteUserServiceImpl(UserService userService) throws RemoteException {
        this.userService = userService;
    }

    // --- Authentication & Session Management ---

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO request) throws RemoteException {
        return userService.register(request);
    }

    @Override
    public LoginResult login(String emailOrPhone, String password, ClientCallback callback) throws RemoteException {
        return userService.login(emailOrPhone, password, callback);
    }

    @Override
    public UserDTO reconnect(long userId, String sessionId, ClientCallback callback) throws RemoteException {
        return userService.reconnect(userId, sessionId, callback);
    }

    @Override
    public void logout(long userId, String sessionId) throws RemoteException {
        userService.logout(userId, sessionId);
    }

    // --- User Profile & Search Management ---

    @Override
    public UserDTO findUserById(long id) throws RemoteException {
        return userService.findUserById(id).orElse(null);
    }

    @Override
    public UserDTO getUserByEmail(String email) throws RemoteException {
        return userService.getUserByEmail(email).orElse(null);
    }

    @Override
    public boolean updateUser(UserDTO userDTO) throws RemoteException {
        return userService.updateUser(userDTO);
    }

    @Override
    public boolean updateUserPassword(long id, String passwordHash) throws RemoteException {
        return userService.updateUserPassword(id, passwordHash);
    }

    @Override
    public byte[] getProfilePicture(String picturePath) throws RemoteException {
        try {
            return userService.getProfilePicture(picturePath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Status Management ---

    @Override
    public boolean updateUserStatus(long userId, UserStatus status) throws RemoteException {
        return userService.updateUserStatus(userId, status);
    }

    @Override
    public UserStatus getUserStatus(long userId) throws RemoteException {
        return userService.getUserStatus(userId);
    }
}