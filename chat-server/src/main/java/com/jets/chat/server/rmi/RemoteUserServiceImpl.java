package com.jets.chat.server.rmi;

import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;
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
    public UserDTO findUserById(long id) throws RemoteException {
        return userService.findUserById(id).orElse(null);
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
    public boolean updateUserStatus(long userId, UserStatus status) throws RemoteException {
        return userService.updateUserStatus(userId, status);
    }

    @Override
    public UserStatus getUserStatus(long userId) throws RemoteException {
        return userService.getUserStatus(userId);
    }
}
