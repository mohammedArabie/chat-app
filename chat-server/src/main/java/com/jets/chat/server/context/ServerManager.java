package com.jets.chat.server.context;

import com.jets.chat.common.rmi.RemoteAnnouncementService;
import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.dao.impl.AnnouncementDaoImpl;
import com.jets.chat.server.rmi.RemoteAnnouncementServiceImpl;
import com.jets.chat.server.service.AnnouncementService;
import com.jets.chat.server.service.impl.AnnouncementServiceImpl;
import com.zaxxer.hikari.HikariDataSource;

import java.rmi.RemoteException;

public class ServerManager {
    private static volatile ServerManager instance;
    private final AnnouncementService announcementService;
    private final RemoteAnnouncementService remoteAnnouncementService;

    public static ServerManager getInstance() throws RemoteException {
        if (instance == null) {
            synchronized (ServerManager.class) {
                if (instance == null) {
                    instance = new ServerManager();
                }
            }
        }
        return instance;
    }

    private ServerManager() throws RemoteException {
        HikariDataSource dataSource = DataSourceConfig.getDataSource();
        AnnouncementDao announcementDao = new AnnouncementDaoImpl(dataSource);
        this.announcementService = new AnnouncementServiceImpl(announcementDao);
        this.remoteAnnouncementService = new RemoteAnnouncementServiceImpl(announcementService);

    }

    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }

    public RemoteAnnouncementService getRemoteAnnouncementService() {
        return remoteAnnouncementService;
    }
}
