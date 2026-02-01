package com.jets.chat.server.service.impl;

import com.jets.chat.server.dao.StatisticsDao;
import com.jets.chat.server.service.ServerStatisticsService;

import java.util.Map;

// ✅ CORRECT: Service layer contains NO database code
// ✅ Delegates ALL data operations to DAO
public class ServerStatisticsServiceImpl implements ServerStatisticsService {
    private final StatisticsDao statisticsDao; // Injected DAO dependency

    // Constructor injection - service knows NOTHING about DataSource
    public ServerStatisticsServiceImpl(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }

    @Override
    public int getOnlineUsersCount() {
        return statisticsDao.getOnlineUsersCount();
    }

    @Override
    public int getOfflineUsersCount() {
        return statisticsDao.getOfflineUsersCount();
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