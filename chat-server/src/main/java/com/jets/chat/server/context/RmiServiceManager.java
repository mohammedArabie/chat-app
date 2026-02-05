package com.jets.chat.server.context;

import com.jets.chat.common.rmi.RemoteFileService;
import com.jets.chat.common.util.ProjectConstants;
import com.jets.chat.server.rmi.RemoteAnnouncementServiceImpl;
import com.jets.chat.server.rmi.RemoteChatServiceImpl;
import com.jets.chat.server.rmi.RemoteContactsServiceImpl;
import com.jets.chat.server.rmi.RemoteUserServiceImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class RmiServiceManager {
    private final ServerManager serverManager;
    private Registry registry;

    private RemoteAnnouncementServiceImpl announcementImpl;
    private RemoteUserServiceImpl userImpl;
    private RemoteContactsServiceImpl contactsImpl;
    private RemoteChatServiceImpl chatImpl;
    private RemoteFileService fileImpl;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public RmiServiceManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    public synchronized void startServices() throws RemoteException {
        if (isRunning.get()) {
            System.out.println("RMI services already running");
            return;
        }

        try {
            try {
                registry = LocateRegistry.getRegistry(ProjectConstants.RMI_SERVICE_PORT);
                registry.list();
            } catch (Exception e) {
                registry = LocateRegistry.createRegistry(ProjectConstants.RMI_SERVICE_PORT);
                System.out.println(
                        "Created RMI registry on port " + ProjectConstants.RMI_SERVICE_PORT);
            }

            announcementImpl = new RemoteAnnouncementServiceImpl(
                    serverManager.getAnnouncementService());
            try {
                UnicastRemoteObject.toStub(announcementImpl);
                System.out.println("Announcement service already exported, reusing...");
            } catch (Exception e) {
                UnicastRemoteObject.exportObject(announcementImpl, 0);
                System.out.println("Exported Announcement service");
            }
            registry.rebind(ProjectConstants.ANNOUNCEMENT_SERVICE, announcementImpl);
            System.out.println("✓ Bound " + ProjectConstants.ANNOUNCEMENT_SERVICE);

            userImpl = new RemoteUserServiceImpl(serverManager.getUserService());
            try {
                UnicastRemoteObject.toStub(userImpl);
                System.out.println("User service already exported, reusing...");
            } catch (Exception e) {
                UnicastRemoteObject.exportObject(userImpl, 0);
                System.out.println("Exported User service");
            }
            registry.rebind(ProjectConstants.USER_SERVICE, userImpl);
            System.out.println("Bound " + ProjectConstants.USER_SERVICE);

            contactsImpl = new RemoteContactsServiceImpl(serverManager.getContactsService());
            try {
                UnicastRemoteObject.toStub(contactsImpl);
                System.out.println("Contacts service already exported, reusing...");
            } catch (Exception e) {
                UnicastRemoteObject.exportObject(contactsImpl, 0);
                System.out.println("Exported Contacts service");
            }
            registry.rebind(ProjectConstants.CONTACTS_SERVICE, contactsImpl);
            System.out.println("Bound " + ProjectConstants.CONTACTS_SERVICE);
            // =============== ADDED THIS SECTION ===============
            // Create and export Chat Service
            chatImpl = new RemoteChatServiceImpl(serverManager.getChatService());

            try {
                UnicastRemoteObject.toStub(chatImpl);
                System.out.println("Chat service already exported, reusing...");
            } catch (Exception e) {
                UnicastRemoteObject.exportObject(chatImpl, 0);
                System.out.println("Exported Chat service");
            }
            registry.rebind(ProjectConstants.CHAT_SERVICE, chatImpl);
            System.out.println("Bound " + ProjectConstants.CHAT_SERVICE);

            fileImpl = serverManager.getRemoteFileService();

            try {
                RemoteFileService fileStub = (RemoteFileService) UnicastRemoteObject
                        .toStub(fileImpl);
                System.out.println("File service already exported, reusing...");
            } catch (Exception e) {
                UnicastRemoteObject.exportObject(fileImpl, 0);
                System.out.println("Exported File service");
            }

            registry.rebind(ProjectConstants.FILE_SERVICE, fileImpl);
            System.out.println("Bound " + ProjectConstants.FILE_SERVICE);

            isRunning.set(true);
            System.out.println("RMI services started successfully");

        } catch (RemoteException e) {
            System.err.println("Failed to start RMI services: " + e.getMessage());
            cleanup();
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error starting RMI services: " + e.getMessage());
            cleanup();
            throw new RemoteException("Failed to start RMI services", e);
        }
    }

    public synchronized void stopServices() {
        if (!isRunning.get()) {
            System.out.println("RMI services not running");
            return;
        }

        try {
            System.out.println("Shutting down RMI services...");

            serverManager.getUserService().clearOnlineUsers();

            if (registry != null) {
                unbindSafe(ProjectConstants.ANNOUNCEMENT_SERVICE);
                unbindSafe(ProjectConstants.USER_SERVICE);
                unbindSafe(ProjectConstants.CONTACTS_SERVICE);
                unbindSafe(ProjectConstants.CHAT_SERVICE);
                unbindSafe(ProjectConstants.FILE_SERVICE);
            }

            announcementImpl = unexportSafe(announcementImpl, "Announcement");
            userImpl = unexportSafe(userImpl, "User");
            contactsImpl = unexportSafe(contactsImpl, "Contacts");
            chatImpl = unexportSafe(chatImpl, "Chat");

            if (fileImpl != null) {
                try {
                    if (UnicastRemoteObject.unexportObject(fileImpl, true)) {
                        System.out.println("Unexported File service");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to unexport file service: " + e.getMessage());
                }
                fileImpl = null;
            }

            isRunning.set(false);
            System.out.println("RMI services stopped successfully");

        } catch (Exception e) {
            System.err.println("Error during RMI shutdown: " + e.getMessage());
            cleanup();
        }
    }

    private void unbindSafe(String name) {
        try {
            registry.unbind(name);
            System.out.println("Unbound " + name);
        } catch (Exception e) {
            System.err.println("Failed to unbind " + name + ": " + e.getMessage());
        }
    }

    private <T> T unexportSafe(T impl, String name) {
        if (impl != null) {
            try {
                if (UnicastRemoteObject.unexportObject((java.rmi.Remote) impl, true)) {
                    System.out.println("Unexported " + name + " service");
                }
            } catch (Exception e) {
                System.err.println("Failed to unexport " + name + " service: " + e.getMessage());
            }
        }
        return null;
    }

    private void cleanup() {
        try {
            forceUnexport(announcementImpl);
            announcementImpl = null;

            forceUnexport(userImpl);
            userImpl = null;

            forceUnexport(contactsImpl);
            contactsImpl = null;

            forceUnexport(chatImpl);
            chatImpl = null;

            if (fileImpl != null) {
                try {
                    UnicastRemoteObject.unexportObject(fileImpl, true);
                } catch (Exception ignored) {
                }
                fileImpl = null;
            }

        } finally {
            isRunning.set(false);
        }
    }

    private void forceUnexport(java.rmi.Remote impl) {
        if (impl != null) {
            try {
                UnicastRemoteObject.unexportObject(impl, true);
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}