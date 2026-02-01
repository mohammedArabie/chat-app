package com.jets.chat.common.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.dto.UserDTO;

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
}
