package com.jets.chat.server.context;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.*;
import com.jets.chat.server.dao.impl.*;
import com.jets.chat.server.service.*;
import com.jets.chat.server.service.impl.*;
import com.zaxxer.hikari.HikariDataSource;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SINGLE RESPONSIBILITY: Business logic coordinator (NO RMI lifecycle)
 * Delegates RMI operations to RmiServiceManager
 */
public class ServerManager {
    private static volatile ServerManager instance;

    // ✅ SHARED ONLINE USERS REGISTRY (single source of truth)
    private final Map<Long, ClientCallback> onlineClients = new ConcurrentHashMap<>();

    // Services
    private final AnnouncementService announcementService;
    private final AdminService adminService;
    private final ServerStatisticsService statisticsService;
    private final UserService userService;

    // ✅ RMI DELEGATE (handles lifecycle ONLY)
    private final RmiServiceManager rmiServiceManager;

    private ServerManager() throws RemoteException {
        HikariDataSource dataSource = DataSourceConfig.getDataSource();

        AnnouncementDao announcementDao = new AnnouncementDaoImpl(dataSource);
        AdminDao adminDao = new AdminDaoImpl(dataSource);
        StatisticsDao statisticsDao = new StatisticsDaoImpl(dataSource);
        UserDao userDao = new UserDaoImpl(dataSource);

        // ✅ PASS SHARED REGISTRY TO SERVICES
        this.announcementService = new AnnouncementServiceImpl(announcementDao);
        this.adminService = new AdminServiceImpl(adminDao);
        this.statisticsService = new ServerStatisticsServiceImpl(statisticsDao);
        this.userService = new UserServiceImpl(userDao);

        this.rmiServiceManager = new RmiServiceManager(this); // Inject self for delegation
    }

    public static ServerManager getInstance() {
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

    public void startServer() throws RemoteException {
        rmiServiceManager.startServices();
    }

    public void stopServer() {
        rmiServiceManager.stopServices();
    }

    public boolean isServerRunning() {
        return rmiServiceManager.isRunning();
    }

    public Map<Long, ClientCallback> getOnlineClients() {
        return onlineClients;
    }

    // Service getters (unchanged)
    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }
    public AdminService getAdminService() {
        return adminService;
    }
    public ServerStatisticsService getStatisticsService() {
        return statisticsService;
    }
    public UserService getUserService() {
        return userService;
    }
}