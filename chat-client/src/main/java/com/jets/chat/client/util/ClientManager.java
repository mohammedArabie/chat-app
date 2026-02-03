package com.jets.chat.client.util;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.rmi.RemoteChatService;
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
    private final RemoteChatService remoteChatService;
    private final ChatViewModel chatViewModel;

    private ClientManager() {
        try {
            Registry registry = LocateRegistry.getRegistry(ProjectConstants.RMI_SERVICE_HOST,
                    ProjectConstants.RMI_SERVICE_PORT);
            this.remoteAnnouncementService = (RemoteAnnouncementService) registry
                    .lookup(ProjectConstants.ANNOUNCEMENT_SERVICE);
            this.remoteUserService = (RemoteUserService) registry
                    .lookup(ProjectConstants.USER_SERVICE);
            this.remoteChatService = (RemoteChatService) registry
                    .lookup(ProjectConstants.CHAT_SERVICE);
            this.chatViewModel = new ChatViewModel();
        } catch (RemoteException e) {
            System.out.println("Failed to connect");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

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

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }

    public RemoteUserService getRemoteUserService() {
        return remoteUserService;
    }

    public RemoteChatService getRemoteChatService() {
        return remoteChatService;
    }

    public ChatViewModel getChatViewModel() {
        return chatViewModel;
    }
}
