package com.jets.chat.client.callback;

import com.jets.chat.common.callback.ClientCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {
    public ClientCallbackImpl() throws RemoteException {
    }
}
