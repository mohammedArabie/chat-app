package com.jets.chat.client.controller;

import com.jets.chat.client.util.ClientManager;
import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.common.enums.UserStatus;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final RemoteUserService remoteUserService;

    public UserService() {
        this.remoteUserService = ClientManager.getInstance().getRemoteUserService();
    }

    public UserDTO getUserById(long userId) {
        try {
            return remoteUserService.findUserById(userId);
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "RMI Error: Could not fetch user", e);
            return null;
        }
    }

    public UserStatus getUserStatus(long userId) {
        try {
            return remoteUserService.getUserStatus(userId);
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "RMI Error: Could not fetch status", e);
            return UserStatus.OFFLINE;
        }
    }

    public boolean saveUserProfile(UserDTO userDTO) {
        try {
            return remoteUserService.updateUser(userDTO);
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "RMI Error: Update failed", e);
            return false;
        }
    }

    public boolean updatePassword(long userId, String passwordHash) {
        try {
            return remoteUserService.updateUserPassword(userId, passwordHash);
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "RMI Error: Password failed", e);
            return false;
        }
    }

    public boolean changeStatus(long userId, UserStatus status) {
        try {
            return remoteUserService.updateUserStatus(userId, status);
        } catch (RemoteException e) {
            logger.log(Level.SEVERE, "RMI Error: Status failed", e);
            return false;
        }
    }
}