package com.jets.chat.server.dao;

import java.util.Map;

public interface StatisticsDao {
    int getOnlineUsersCount();
    int getOfflineUsersCount();
    Map<String, Long> getUserGenderStatistics();
    Map<String, Long> getUserCountryStatistics();
    long getTotalUsers();
    long getTotalMessages();
    long getTotalChats();
}