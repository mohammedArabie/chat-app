package com.jets.chat.server.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.server.service.UserService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
}
