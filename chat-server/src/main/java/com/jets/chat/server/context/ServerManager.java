package com.jets.chat.server.context;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.rmi.RemoteChatService;
import com.jets.chat.common.rmi.RemoteContactsService;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.AdminDao;
import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.dao.ContactsDao;
import com.jets.chat.server.dao.StatisticsDao;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.dao.impl.AdminDaoImpl;
import com.jets.chat.server.dao.impl.AnnouncementDaoImpl;
import com.jets.chat.server.dao.impl.ContactsDaoImpl;
import com.jets.chat.server.dao.impl.StatisticsDaoImpl;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.rmi.RemoteAnnouncementServiceImpl;
import com.jets.chat.server.rmi.RemoteChatServiceImpl;
import com.jets.chat.server.rmi.RemoteContactsServiceImpl;
import com.jets.chat.server.rmi.RemoteUserServiceImpl;
import com.jets.chat.server.service.AdminService;
import com.jets.chat.server.service.AnnouncementService;
import com.jets.chat.server.service.ContactsService;
import com.jets.chat.server.service.ServerStatisticsService;
import com.jets.chat.server.service.UserService;
import com.jets.chat.server.service.impl.AdminServiceImpl;
import com.jets.chat.server.service.impl.AnnouncementServiceImpl;
import com.jets.chat.server.service.impl.ContactsServiceImpl;
import com.jets.chat.server.service.impl.ServerStatisticsServiceImpl;
import com.jets.chat.server.service.impl.UserServiceImpl;
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

    // Services
    private final AnnouncementService announcementService;
    private final AdminService adminService;
    private final ServerStatisticsService statisticsService;
    private final UserService userService;
    private final ContactsService contactsService;

    // Remote Services (RMI)
    private final RemoteAnnouncementService remoteAnnouncementService;
    private final RemoteUserService remoteUserService;
    private final RemoteChatService remoteChatService;
    private final RemoteContactsService remoteContactsService;

    // Managers & Shared State
    private final RmiServiceManager rmiServiceManager;
    private final Map<Long, ClientCallback> onlineClients = new ConcurrentHashMap<>();

    private ServerManager() throws RemoteException {
        // 1. Initialize DataSource
        HikariDataSource dataSource = DataSourceConfig.getDataSource();

        // 2. Initialize DAOs
        AnnouncementDao announcementDao = new AnnouncementDaoImpl(dataSource);
        AdminDao adminDao = new AdminDaoImpl(dataSource);
        StatisticsDao statisticsDao = new StatisticsDaoImpl(dataSource);
        UserDao userDao = new UserDaoImpl(dataSource);
        ContactsDao contactsDao = new ContactsDaoImpl(dataSource);

        // 3. Initialize Services
        this.announcementService = new AnnouncementServiceImpl(announcementDao);
        this.userService = new UserServiceImpl(userDao);
        this.adminService = new AdminServiceImpl(adminDao);
        this.statisticsService = new ServerStatisticsServiceImpl(statisticsDao);
        this.contactsService = new ContactsServiceImpl(contactsDao, userDao);

        // 4. Initialize Remote Services
        this.remoteAnnouncementService = new RemoteAnnouncementServiceImpl(announcementService);
        this.remoteUserService = new RemoteUserServiceImpl(userService);
        this.remoteChatService = new RemoteChatServiceImpl();
        this.remoteContactsService = new RemoteContactsServiceImpl(contactsService);

        // 5. Initialize RMI Manager (Delegation)
        this.rmiServiceManager = new RmiServiceManager(this);
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            synchronized (ServerManager.class) {
                if (instance == null) {
                    try {
                        instance = new ServerManager();
                    } catch (RemoteException e) {
                        System.err.println("Fatal Error: Could not initialize ServerManager");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return instance;
    }

    // --- Server Lifecycle Control ---

    public void startServer() throws RemoteException {
        if (rmiServiceManager != null) {
            rmiServiceManager.startServices();
            System.out.println("Server services started successfully.");
        }
    }

    public void stopServer() {
        if (rmiServiceManager != null) {
            rmiServiceManager.stopServices();
            System.out.println("Server services stopped.");
        }
    }

    public boolean isServerRunning() {
        return rmiServiceManager != null && rmiServiceManager.isRunning();
    }

    // --- Getters ---

    public Map<Long, ClientCallback> getOnlineClients() {
        return onlineClients;
    }

    public UserService getUserService() {
        return userService;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }

    public ContactsService getContactsService() {
        return contactsService;
    }

    public ServerStatisticsService getStatisticsService() {
        return statisticsService;
    }

    // --- RMI Getters ---

    public RemoteUserService getRemoteUserService() {
        return remoteUserService;
    }

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }

    public RemoteChatService getRemoteChatService() {
        return remoteChatService;
    }

    public RemoteContactsService getRemoteContactsService() {
        return remoteContactsService;
    }

    public RmiServiceManager getRmiServiceManager() {
        return rmiServiceManager;
    }
}