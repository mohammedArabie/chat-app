package com.jets.chat.server.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.dto.UserDTO;
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

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO request) throws RemoteException {
        return userService.register(request);
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

    public LoginResult login(String emailOrPhone, String password, ClientCallback callback)
            throws RemoteException {
        return userService.login(emailOrPhone, password, callback);
    }

    @Override
    public UserDTO reconnect(long userId, String sessionId, ClientCallback callback)
            throws RemoteException {
        return userService.reconnect(userId, sessionId, callback);
    }

    @Override
    public void logout(long userId, String sessionId) throws RemoteException {
        userService.logout(userId, sessionId);
    }

    @Override
    public UserDTO getUserByEmail(String email) throws RemoteException {
        Optional<UserDTO> user = userService.getUserByEmail(email);
        if (user.isEmpty())
            return null;
        return user.get();
    }

    @Override
    public UserDTO getUserById(long userId) throws RemoteException {
        Optional<UserDTO> user = userService.getUserById(userId);
        if (user.isEmpty())
            return null;
        return user.get();
    }

    @Override
    public void updateStatus(long userId, UserStatus status) throws RemoteException {
        userService.updateStatus(userId, status);
        userService.notifyContactsOfStatusChange(userId, status);
    }

    @Override
    public UserStatus getUserStatus(long userId) throws RemoteException {
        return userService.getUserStatus(userId);
    }

    @Override
    public UserDTO updateUserProfile(long userId, String displayName, String email)
            throws RemoteException {
        return userService.updateUserProfile(userId, displayName, email);
    }

    @Override
    public boolean isEmailExists(String email) throws RemoteException {
        return userService.isEmailExists(email);
    }
}
