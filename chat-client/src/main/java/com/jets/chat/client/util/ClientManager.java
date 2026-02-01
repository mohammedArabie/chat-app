package com.jets.chat.client.util;

import com.jets.chat.client.callback.ClientAnnouncementCallback;
import com.jets.chat.common.rmi.AnnouncementCallback;
import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.common.util.ProjectConstants;
import javafx.stage.Stage;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientManager {
    private static ClientManager instance;
    private AnnouncementCallback callback;
    private String sessionId;
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
    public void registerAnnouncementCallback(String sessionId, Stage primaryStage)
            throws RemoteException {
        this.sessionId = sessionId;
        this.callback = new ClientAnnouncementCallback(primaryStage);
        remoteAnnouncementService.registerCallback(sessionId, callback);
    }

    public void unregisterAnnouncementCallback() {
        if (sessionId != null && callback != null) {
            try {
                remoteAnnouncementService.unregisterCallback(sessionId);
                java.rmi.server.UnicastRemoteObject.unexportObject(callback, true);
            } catch (Exception e) {
                // Ignore
            }
            this.sessionId = null;
            this.callback = null;
        }
    }

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }
    public RemoteUserService getRemoteUserService() {
        return remoteUserService;
    }
}
