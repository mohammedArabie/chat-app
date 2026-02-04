package com.jets.chat.server;

import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.rmi.RemoteContactsService;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.common.util.ProjectConstants;
import com.jets.chat.server.context.ServerManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            ServerManager serverManager = ServerManager.getInstance();

            RemoteAnnouncementService remoteAnnouncementService = serverManager
                    .getRemoteAnnouncementService();

            RemoteUserService remoteUserService = serverManager.getRemoteUserService();

            RemoteContactsService remoteContactsService = serverManager.getRemoteContactsService();

            Registry registry = LocateRegistry.createRegistry(ProjectConstants.RMI_SERVICE_PORT);

            registry.rebind(ProjectConstants.ANNOUNCEMENT_SERVICE, remoteAnnouncementService);

            registry.rebind(ProjectConstants.CHAT_SERVICE, serverManager.getRemoteChatService());

            registry.rebind(ProjectConstants.USER_SERVICE, remoteUserService);

            registry.rebind(ProjectConstants.CONTACTS_SERVICE, remoteContactsService);

            System.out.println("Server Running");
            serverManager.startServer();

            // Needed when using exec:java to run the server
            // Not needed when using java to run
            // Thread.currentThread().join();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
