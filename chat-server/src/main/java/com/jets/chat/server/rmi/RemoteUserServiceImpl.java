package com.jets.chat.server.rmi;

import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.server.service.UserService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;

public class RemoteUserServiceImpl extends UnicastRemoteObject implements RemoteUserService {

    private final UserService userService;

    public RemoteUserServiceImpl(UserService userService) throws RemoteException {
        this.userService = userService;
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO request) throws RemoteException {
        return userService.register(request);
    }
}
