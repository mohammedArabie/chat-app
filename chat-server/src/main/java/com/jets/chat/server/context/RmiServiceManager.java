package com.jets.chat.server.context;

import com.jets.chat.common.util.ProjectConstants;
import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.server.rmi.RemoteAnnouncementServiceImpl;
import com.jets.chat.server.rmi.RemoteUserServiceImpl;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

// Manages RMI lifecycle ONLY (bind/unbind/export)

public class RmiServiceManager {
    private final ServerManager serverManager; // Delegates business logic
    private Registry registry;
    private RemoteAnnouncementServiceImpl announcementImpl; // Store the implementation
    private RemoteUserServiceImpl userImpl; // Store the implementation
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
            // Create/get registry
            try {
                registry = LocateRegistry.getRegistry(ProjectConstants.RMI_SERVICE_PORT);
                registry.list(); // Test connection
            } catch (Exception e) {
                registry = LocateRegistry.createRegistry(ProjectConstants.RMI_SERVICE_PORT);
                System.out.println(
                        "Created RMI registry on port " + ProjectConstants.RMI_SERVICE_PORT);
            }

            // Create and export Announcement Service (check if already exported)
            announcementImpl = new RemoteAnnouncementServiceImpl(
                    serverManager.getAnnouncementService());

            // Check if already exported before exporting
            try {
                RemoteAnnouncementService announcementStub = (RemoteAnnouncementService) UnicastRemoteObject
                        .toStub(announcementImpl);
                // If we get here, it's already exported
                System.out.println("Announcement service already exported, reusing...");
            } catch (Exception e) {
                // Not exported yet, so export it
                UnicastRemoteObject.exportObject(announcementImpl, 0);
                System.out.println("Exported Announcement service");
            }

            registry.rebind(ProjectConstants.ANNOUNCEMENT_SERVICE, announcementImpl);
            System.out.println("✓ Bound " + ProjectConstants.ANNOUNCEMENT_SERVICE);

            // Create and export User Service
            userImpl = new RemoteUserServiceImpl(serverManager.getUserService());

            // Check if already exported before exporting
            try {
                RemoteUserService userStub = (RemoteUserService) UnicastRemoteObject
                        .toStub(userImpl);
                // If we get here, it's already exported
                System.out.println("User service already exported, reusing...");
            } catch (Exception e) {
                // Not exported yet, so export it
                UnicastRemoteObject.exportObject(userImpl, 0);
                System.out.println("Exported User service");
            }

            registry.rebind(ProjectConstants.USER_SERVICE, userImpl);
            System.out.println("Bound " + ProjectConstants.USER_SERVICE);

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

            // Clear callbacks BEFORE unbinding (prevents broadcast to dead
            // clients)
            serverManager.getUserService().clearOnlineUsers();

            // Unbind services
            if (registry != null) {
                try {
                    registry.unbind(ProjectConstants.ANNOUNCEMENT_SERVICE);
                    System.out.println("Unbound " + ProjectConstants.ANNOUNCEMENT_SERVICE);
                } catch (Exception e) {
                    System.err.println("Failed to unbind announcement service: " + e.getMessage());
                }
                try {
                    registry.unbind(ProjectConstants.USER_SERVICE);
                    System.out.println("Unbound " + ProjectConstants.USER_SERVICE);
                } catch (Exception e) {
                    System.err.println("Failed to unbind user service: " + e.getMessage());
                }
            }

            // Unexport remote objects gracefully
            if (announcementImpl != null) {
                try {
                    if (UnicastRemoteObject.unexportObject(announcementImpl, true)) {
                        System.out.println("Unexported Announcement service");
                    }
                } catch (Exception e) {
                    System.err
                            .println("Failed to unexport announcement service: " + e.getMessage());
                }
                announcementImpl = null;
            }

            if (userImpl != null) {
                try {
                    if (UnicastRemoteObject.unexportObject(userImpl, true)) {
                        System.out.println("Unexported User service");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to unexport user service: " + e.getMessage());
                }
                userImpl = null;
            }

            isRunning.set(false);
            System.out.println("RMI services stopped successfully");

        } catch (Exception e) {
            System.err.println("Error during RMI shutdown: " + e.getMessage());
            cleanup();
        }
    }

    private void cleanup() {
        try {
            // Force cleanup
            if (announcementImpl != null) {
                try {
                    UnicastRemoteObject.unexportObject(announcementImpl, true);
                } catch (Exception ignored) {
                }
                announcementImpl = null;
            }

            if (userImpl != null) {
                try {
                    UnicastRemoteObject.unexportObject(userImpl, true);
                } catch (Exception ignored) {
                }
                userImpl = null;
            }
        } finally {
            isRunning.set(false);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}