package com.jets.chat.server.service.impl;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.StatisticsDao;
import com.jets.chat.server.service.ServerStatisticsService;

import java.util.Map;

public class ServerStatisticsServiceImpl implements ServerStatisticsService {
    private final StatisticsDao statisticsDao; // Injected DAO dependency

    // Constructor injection - service knows NOTHING about DataSource
    public ServerStatisticsServiceImpl(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }

    @Override
    public int getOnlineUsersCount() {
        Map<Long, ClientCallback> onlineClients = ServerManager.getInstance().getOnlineClients();
        return onlineClients.size();
    }

    @Override
    public int getOfflineUsersCount() {
        long totalUsers = statisticsDao.getTotalUsers();
        int onlineUsers = getOnlineUsersCount();
        return (int) (totalUsers - onlineUsers);
    }

    @Override
    public Map<String, Long> getUserGenderStatistics() {
        return statisticsDao.getUserGenderStatistics();
    }

    @Override
    public Map<String, Long> getUserCountryStatistics() {
        return statisticsDao.getUserCountryStatistics();
    }

    @Override
    public long getTotalUsers() {
        return statisticsDao.getTotalUsers();
    }

    @Override
    public long getTotalMessages() {
        return statisticsDao.getTotalMessages();
    }

    @Override
    public long getTotalChats() {
        return statisticsDao.getTotalChats();
    }
}