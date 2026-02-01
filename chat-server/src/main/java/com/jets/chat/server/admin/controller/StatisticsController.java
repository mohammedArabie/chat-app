package com.jets.chat.server.admin.controller;

import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.service.ServerStatisticsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import java.util.Comparator;

public class StatisticsController {
    @FXML
    private Label onlineCount, offlineCount, totalCount, messageCount, chatCount, lastUpdated;
    @FXML
    private PieChart genderChart;
    @FXML
    private BarChart<String, Number> countryChart;

    private ServerManager serverManager;
    private ServerStatisticsService statsService;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss",
            Locale.getDefault());

    public void init(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.statsService = serverManager.getStatisticsService();
        refreshStatistics();
    }

    @FXML
    public void refreshStatistics() {
        // Show loading state
        onlineCount.setText("...");
        offlineCount.setText("...");
        totalCount.setText("...");
        messageCount.setText("...");
        chatCount.setText("...");

        // Load stats in background thread
        new Thread(() -> {
            int online = statsService.getOnlineUsersCount();
            int offline = statsService.getOfflineUsersCount();
            long total = statsService.getTotalUsers();
            long messages = statsService.getTotalMessages();
            long chats = statsService.getTotalChats();
            Map<String, Long> genderStats = statsService.getUserGenderStatistics();
            Map<String, Long> countryStats = statsService.getUserCountryStatistics();

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                onlineCount.setText(String.valueOf(online));
                offlineCount.setText(String.valueOf(offline));
                totalCount.setText(String.valueOf(total));
                messageCount.setText(String.format("%,d", messages));
                chatCount.setText(String.format("%,d", chats));

                updateGenderChart(genderStats);
                updateCountryChart(countryStats);
                lastUpdated.setText("Last updated: " + timeFormat.format(new Date()));
            });
        }).start();
    }

    private void updateGenderChart(Map<String, Long> genderStats) {
        genderChart.getData().clear();
        genderChart.setTitle("Gender Distribution");

        genderStats.forEach((gender, count) -> {
            String label = gender != null
                    ? gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase()
                    : "Not Specified";
            PieChart.Data slice = new PieChart.Data(label + " (" + count + ")", count);
            genderChart.getData().add(slice);
        });
    }

    private void updateCountryChart(Map<String, Long> countryStats) {

        countryChart.getData().clear();
        countryChart.setTitle("Top Countries");

        // Force horizontal labels
        CategoryAxis xAxis = (CategoryAxis) countryChart.getXAxis();
        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);
        xAxis.setTickMarkVisible(false);

        var series = new XYChart.Series<String, Number>();
        series.setName("Users");

        // Sort DESC and take top 5
        countryStats.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().trim().isEmpty())
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5).forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        countryChart.getData().add(series);
    }

}