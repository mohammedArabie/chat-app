package com.jets.chat.server.context;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.common.rmi.RemoteUserService;
import com.jets.chat.common.rmi.RemoteChatService;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.dao.impl.AnnouncementDaoImpl;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.rmi.RemoteAnnouncementServiceImpl;
import com.jets.chat.server.rmi.RemoteChatServiceImpl;
import com.jets.chat.server.rmi.RemoteUserServiceImpl;
import com.jets.chat.server.service.AnnouncementService;
import com.jets.chat.server.service.UserService;
import com.jets.chat.server.service.impl.AnnouncementServiceImpl;
import com.jets.chat.server.service.impl.UserServiceImpl;
import com.zaxxer.hikari.HikariDataSource;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    private static volatile ServerManager instance;
    private final AnnouncementService announcementService;
    private final RemoteAnnouncementService remoteAnnouncementService;
    private final RemoteUserService remoteUserService;
    private final UserService userService;
    private final Map<Long, ClientCallback> onlineClients = new ConcurrentHashMap<>();
    private final RemoteChatService remoteChatService;

    private ServerManager() throws RemoteException {
        HikariDataSource dataSource = DataSourceConfig.getDataSource();
        AnnouncementDao announcementDao = new AnnouncementDaoImpl(dataSource);
        UserDao userDao = new UserDaoImpl(dataSource);
        this.announcementService = new AnnouncementServiceImpl(announcementDao);
        this.remoteAnnouncementService = new RemoteAnnouncementServiceImpl(announcementService);
        this.remoteChatService = new RemoteChatServiceImpl();
        this.userService = new UserServiceImpl(userDao);
        this.remoteUserService = new RemoteUserServiceImpl(userService);
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

    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }

    public UserService getUserService() {
        return userService;
    }

    public RemoteUserService getRemoteUserService() {
        return remoteUserService;
    }

    public Map<Long, ClientCallback> getOnlineClients() {
        return onlineClients;
    }

    public RemoteChatService getRemoteChatService() {
        return remoteChatService;
    }
}
