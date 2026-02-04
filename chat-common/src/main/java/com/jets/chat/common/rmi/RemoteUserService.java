package com.jets.chat.common.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.*;
import com.jets.chat.common.enums.UserStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteUserService extends Remote {

    RegisterResponseDTO register(RegisterRequestDTO request) throws RemoteException;

    byte[] getProfilePicture(String picturePath) throws RemoteException;

    LoginResult login(String emailOrPhone, String password, ClientCallback callback)
            throws RemoteException;

    UserDTO reconnect(long userId, String sessionId, ClientCallback callback)
            throws RemoteException;

    void logout(long userId, String sessionId) throws RemoteException;

    UserDTO getUserByEmail(String email) throws RemoteException;

    void updateStatus(long userId, UserStatus status) throws RemoteException;

    UserStatus getUserStatus(long userId) throws RemoteException;
}
