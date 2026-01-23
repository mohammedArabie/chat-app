package com.jets.chat.common.rmi;

import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteUserService extends Remote {

    RegisterResponseDTO register(RegisterRequestDTO request) throws RemoteException;
}
