package com.jets.chat.client.util;

import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.common.util.ProjectConstants;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientManager {
    private static ClientManager instance;

    private final RemoteAnnouncementService remoteAnnouncementService;

    private final RemoteUserService remoteUserService;

    public static ClientManager getInstance() {
        if (instance == null) {
            synchronized (ClientManager.class) {
                if (instance == null) {
                    instance = new ClientManager();
                }
            }
        }
        return instance;
    }

    private ClientManager() {
        try {
            Registry registry = LocateRegistry.getRegistry(ProjectConstants.RMI_SERVICE_HOST,
                    ProjectConstants.RMI_SERVICE_PORT);
            this.remoteAnnouncementService = (RemoteAnnouncementService) registry
                    .lookup(ProjectConstants.ANNOUNCEMENT_SERVICE);
            this.remoteUserService = (RemoteUserService) registry
                    .lookup(ProjectConstants.USER_SERVICE);

        } catch (RemoteException e) {
            System.out.println("Failed to connect");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }
    public RemoteUserService getRemoteUserService() {
        return remoteUserService;
    }
}
