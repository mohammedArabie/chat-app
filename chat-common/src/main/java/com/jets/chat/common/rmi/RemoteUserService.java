package com.jets.chat.common.rmi;

import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteUserService extends Remote {

    UserDTO findUserById(long id) throws RemoteException;

    boolean updateUser(UserDTO userDTO) throws RemoteException;

    boolean updateUserPassword(long id, String passwordHash) throws RemoteException;

    boolean updateUserStatus(long userId, UserStatus status) throws RemoteException;

    UserStatus getUserStatus(long userId) throws RemoteException;
}
