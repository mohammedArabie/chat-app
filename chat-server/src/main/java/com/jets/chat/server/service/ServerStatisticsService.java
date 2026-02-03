package com.jets.chat.server.service;

import java.util.Map;

public interface ServerStatisticsService {
    int getOnlineUsersCount();
    int getOfflineUsersCount();
    Map<String, Long> getUserGenderStatistics();
    Map<String, Long> getUserCountryStatistics();
    long getTotalUsers();
    long getTotalMessages();
    long getTotalChats();
}