package com.jets.chat.server.context;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.rmi.*;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.*;
import com.jets.chat.server.dao.impl.*;
import com.jets.chat.server.rmi.RemoteAnnouncementServiceImpl;
import com.jets.chat.server.rmi.RemoteChatServiceImpl;
import com.jets.chat.server.rmi.RemoteContactsServiceImpl;
import com.jets.chat.server.rmi.RemoteUserServiceImpl;
import com.jets.chat.server.service.*;
import com.jets.chat.server.service.impl.*;
import com.jets.chat.server.rmi.*;
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

public class ServerManager {
    private static volatile ServerManager instance;
    private final AnnouncementService announcementService;
    private final AdminService adminService;
    private final ServerStatisticsService statisticsService;
    private final UserService userService;
    private final RemoteAnnouncementService remoteAnnouncementService;
    private final RemoteUserService remoteUserService;
    private final Map<Long, ClientCallback> onlineClients = new ConcurrentHashMap<>();
    private final RemoteChatService remoteChatService;
    private final RmiServiceManager rmiServiceManager;
    private final ContactsService contactsService;
    private final RemoteContactsService remoteContactsService;
    private final ChatService chatService;
    private final RemoteFileService remoteFileService;

    private ServerManager() throws RemoteException {
        HikariDataSource dataSource = DataSourceConfig.getDataSource();
        AnnouncementDao announcementDao = new AnnouncementDaoImpl(dataSource);
        AdminDao adminDao = new AdminDaoImpl(dataSource);
        StatisticsDao statisticsDao = new StatisticsDaoImpl(dataSource);
        UserDao userDao = new UserDaoImpl(dataSource);
        ChatDao chatDao = new ChatDaoImpl(dataSource);
        FileDao fileDao = new FileDaoImpl(dataSource);
        ContactsDao contactsDao = new ContactsDaoImpl(dataSource);
        this.announcementService = new AnnouncementServiceImpl(announcementDao);
        this.remoteAnnouncementService = new RemoteAnnouncementServiceImpl(announcementService);
        this.userService = new UserServiceImpl(userDao, contactsDao);
        this.remoteUserService = new RemoteUserServiceImpl(userService);
        this.adminService = new AdminServiceImpl(adminDao);
        this.statisticsService = new ServerStatisticsServiceImpl(statisticsDao);
        this.rmiServiceManager = new RmiServiceManager(this); // Inject self for delegation

        // Initialize ContactsService
        this.contactsService = new ContactsServiceImpl(contactsDao, userDao, chatDao);
        this.remoteContactsService = new RemoteContactsServiceImpl(contactsService);

        // Initialize ChatService
        MessageDao messageDao = new MessageDaoImpl(dataSource);
        this.chatService = new ChatServiceImpl(chatDao, messageDao, userDao);
        this.remoteChatService = new RemoteChatServiceImpl(chatService);
        this.remoteFileService = new RemoteFileServiceImpl(fileDao);
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

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }

    public RmiServiceManager getRmiServiceManager() {
        return rmiServiceManager;
    }

    public boolean isServerRunning() {
        return rmiServiceManager.isRunning();
    }

    public Map<Long, ClientCallback> getOnlineClients() {
        return onlineClients;
    }

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

    public RemoteUserService getRemoteUserService() {
        return remoteUserService;
    }

    public RemoteChatService getRemoteChatService() {
        return remoteChatService;
    }

    public ContactsService getContactsService() {
        return contactsService;
    }

    public RemoteContactsService getRemoteContactsService() {
        return remoteContactsService;
    }

    public ChatService getChatService() {
        return chatService;
    }

    public RemoteFileService getRemoteFileService() {
        return remoteFileService;
    }
}
