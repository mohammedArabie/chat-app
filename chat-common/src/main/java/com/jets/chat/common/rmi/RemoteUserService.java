package com.jets.chat.common.rmi;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.*;
import com.jets.chat.common.enums.UserStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface الشامل لخدمات المستخدم عبر RMI.
 * هذا الملف يجب أن يكون متطابقاً عند السيرفر والكلينت.
 */
public interface RemoteUserService extends Remote {

    // --- Authentication & Session ---
    RegisterResponseDTO register(RegisterRequestDTO request) throws RemoteException;

    LoginResult login(String emailOrPhone, String password, ClientCallback callback) throws RemoteException;

    UserDTO reconnect(long userId, String sessionId, ClientCallback callback) throws RemoteException;

    void logout(long userId, String sessionId) throws RemoteException;


    // --- User Profile & Search ---
    UserDTO findUserById(long id) throws RemoteException;

    UserDTO getUserByEmail(String email) throws RemoteException;

    boolean updateUser(UserDTO userDTO) throws RemoteException;

    boolean updateUserPassword(long id, String passwordHash) throws RemoteException;

    byte[] getProfilePicture(String picturePath) throws RemoteException;


    // --- Status Management ---
    boolean updateUserStatus(long userId, UserStatus status) throws RemoteException;

    UserStatus getUserStatus(long userId) throws RemoteException;
}