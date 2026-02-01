package com.jets.chat.server.context;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.util.ProjectConstants;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.AdminDao;
import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.dao.StatisticsDao;
import com.jets.chat.server.dao.impl.AdminDaoImpl;
import com.jets.chat.server.dao.impl.AnnouncementDaoImpl;
import com.jets.chat.server.dao.impl.StatisticsDaoImpl;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.rmi.RemoteAnnouncementServiceImpl;
import com.jets.chat.server.service.AdminService;
import com.jets.chat.server.rmi.RemoteUserServiceImpl;
import com.jets.chat.server.service.AnnouncementService;
import com.jets.chat.server.service.ServerStatisticsService;
import com.jets.chat.server.service.impl.AdminServiceImpl;
import com.jets.chat.server.service.UserService;
import com.jets.chat.server.service.impl.AnnouncementServiceImpl;
import com.jets.chat.server.service.impl.ServerStatisticsServiceImpl;
import com.jets.chat.server.service.impl.UserServiceImpl;
import com.zaxxer.hikari.HikariDataSource;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerManager {
    private static volatile ServerManager instance;
    private final AnnouncementService announcementService;
    private RemoteAnnouncementService remoteAnnouncementService;
    private final AdminService adminService;
    private final ServerStatisticsService statisticsService;
    private final RemoteUserService remoteUserService;
    private final UserService userService;
    private final Map<Long, ClientCallback> onlineClients = new ConcurrentHashMap<>();

    private Registry registry;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private boolean isExported = false;

    public static ServerManager getInstance() throws RemoteException {
        if (instance == null) {
            synchronized (ServerManager.class) {
                if (instance == null) {
                    try {
                        instance = new ServerManager();
                    } catch (RemoteException e) {
                        System.out.println("Error Starting the server");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return instance;
    }

    private ServerManager() throws RemoteException {
        HikariDataSource dataSource = DataSourceConfig.getDataSource();
        AnnouncementDao announcementDao = new AnnouncementDaoImpl(dataSource);
        AdminDao adminDao = new AdminDaoImpl(dataSource);
        StatisticsDao statisticsDao = new StatisticsDaoImpl(dataSource);
        StatisticsDao statisticsDao = new StatisticsDaoImpl(dataSource);  // NEW
        UserDao userDao = new UserDaoImpl(dataSource);
        this.announcementService = new AnnouncementServiceImpl(announcementDao);
        this.adminService = new AdminServiceImpl(adminDao);
        this.statisticsService = new ServerStatisticsServiceImpl(statisticsDao);
        this.remoteAnnouncementService = new RemoteAnnouncementServiceImpl(announcementService);
        this.userService = new UserServiceImpl(userDao);
        this.remoteUserService = new RemoteUserServiceImpl(userService);

    }

    public synchronized void startServer() throws RemoteException {
        if (isRunning.get()) {
            System.out.println("Server already running");
            return;
        }

        if (isExported) {
            try {
                UnicastRemoteObject.unexportObject(remoteAnnouncementService, true);
            } catch (Exception e) {
            }
            remoteAnnouncementService = null;
            isExported = false;
        }

        try {
            RemoteAnnouncementServiceImpl serviceImpl = new RemoteAnnouncementServiceImpl(
                    announcementService);
            remoteAnnouncementService = serviceImpl;

            try {
                UnicastRemoteObject.exportObject(remoteAnnouncementService, 0);
                isExported = true;
            } catch (java.rmi.server.ExportException e) {
                remoteAnnouncementService = (RemoteAnnouncementService) UnicastRemoteObject
                        .toStub(remoteAnnouncementService);
                isExported = true;
            }

            try {
                registry = LocateRegistry.getRegistry(ProjectConstants.RMI_SERVICE_PORT);
                registry.list();
            } catch (Exception e) {
                registry = LocateRegistry.createRegistry(ProjectConstants.RMI_SERVICE_PORT);
            }

            registry.rebind(ProjectConstants.ANNOUNCEMENT_SERVICE, remoteAnnouncementService);
            isRunning.set(true);

        } catch (RemoteException e) {
            if (remoteAnnouncementService != null && isExported) {
                try {
                    UnicastRemoteObject.unexportObject(remoteAnnouncementService, true);
                } catch (Exception ex) {
                }
                remoteAnnouncementService = null;
                isExported = false;
            }
            throw e;
        }
    }

    public synchronized void stopServer() {
        if (!isRunning.get()) {
            System.out.println("Server not running");
            return;
        }
        announcementService.clearCallbacks();
        try {
            if (registry != null) {
                try {
                    registry.unbind(ProjectConstants.ANNOUNCEMENT_SERVICE);
                } catch (Exception e) {
                }
            }

            if (remoteAnnouncementService != null && isExported) {
                try {
                    UnicastRemoteObject.unexportObject(remoteAnnouncementService, true);
                } catch (Exception e) {
                }
                remoteAnnouncementService = null;
                isExported = false;
            }

            isRunning.set(false);
            System.out.println("✓✓✓ ANNOUNCEMENTS STOPPED ✓✓✓");
        } catch (Exception e) {
            isRunning.set(false);
            remoteAnnouncementService = null;
            isExported = false;
        }
    }

    public boolean isServerRunning() {
        return isRunning.get();
    }

    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public ServerStatisticsService getStatisticsService() {
        return statisticsService;
    }
    public ServerStatisticsService getStatisticsService() { return statisticsService; }

    public UserService getUserService() {
        return userService;
    }

    public RemoteUserService getRemoteUserService() {
        return remoteUserService;
    }

    public Map<Long, ClientCallback> getOnlineClients() {
        return onlineClients;
    }
}