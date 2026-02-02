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

    private boolean isInitialized = false;
    private boolean isFirstLoad = true;


    @FXML
    public void initialize() {

        if (genderChart != null) {
            genderChart.setAnimated(true);
            genderChart.setTitle("Loading Statistics...");

            PieChart.Data placeholder = new PieChart.Data("Loading...", 100);
            genderChart.getData().add(placeholder);
        }

        if (countryChart != null) {
            countryChart.setAnimated(true);
            countryChart.setTitle("Loading Statistics...");

            CategoryAxis xAxis = (CategoryAxis) countryChart.getXAxis();
            xAxis.setTickLabelRotation(0);
            xAxis.setTickLabelGap(10);


            var series = new XYChart.Series<String, Number>();
            series.setName("Loading...");
            series.getData().add(new XYChart.Data<>("Please wait", 100));
            countryChart.getData().add(series);
        }

        // 初始化标签
        if (onlineCount != null) onlineCount.setText("0");
        if (offlineCount != null) offlineCount.setText("0");
        if (totalCount != null) totalCount.setText("0");
        if (messageCount != null) messageCount.setText("0");
        if (chatCount != null) chatCount.setText("0");
        if (lastUpdated != null) lastUpdated.setText("Last updated: --:--:--");

        isInitialized = true;
    }

    public void init(ServerManager serverManager) {
        if (serverManager == null) {
            throw new IllegalArgumentException("ServerManager cannot be null");
        }

        this.serverManager = serverManager;
        this.statsService = serverManager.getStatisticsService();

        if (statsService == null) {
            throw new IllegalStateException("StatisticsService is not available");
        }

        Platform.runLater(() -> {

            javafx.animation.PauseTransition initialDelay =
                    new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            initialDelay.setOnFinished(e -> refreshStatistics());
            initialDelay.play();
        });
    }

    @FXML
    public void refreshStatistics() {
        if (!isInitialized || statsService == null) {
            System.err.println("Controller not properly initialized");
            return;
        }


        Platform.runLater(() -> {

            animateLabelUpdate(onlineCount, "...");
            animateLabelUpdate(offlineCount, "...");
            animateLabelUpdate(totalCount, "...");
            animateLabelUpdate(messageCount, "...");
            animateLabelUpdate(chatCount, "...");


            if (!isFirstLoad) {

                if (genderChart != null) {
                    genderChart.getData().clear();
                    genderChart.setTitle("Updating...");
                }

                if (countryChart != null) {
                    countryChart.getData().clear();
                    countryChart.setTitle("Updating...");
                }
            }
        });


        Thread loadThread = new Thread(() -> {
            try {

                int online = statsService.getOnlineUsersCount();
                int offline = statsService.getOfflineUsersCount();
                long total = statsService.getTotalUsers();
                long messages = statsService.getTotalMessages();
                long chats = statsService.getTotalChats();
                Map<String, Long> genderStats = statsService.getUserGenderStatistics();
                Map<String, Long> countryStats = statsService.getUserCountryStatistics();


                Platform.runLater(() -> {
                    try {

                        animateLabelUpdate(onlineCount, String.valueOf(online));
                        animateLabelUpdate(offlineCount, String.valueOf(offline));
                        animateLabelUpdate(totalCount, String.valueOf(total));
                        animateLabelUpdate(messageCount, String.format("%,d", messages));
                        animateLabelUpdate(chatCount, String.format("%,d", chats));

                        updateGenderChart(genderStats);
                        updateCountryChart(countryStats);


                        lastUpdated.setText("Last updated: " + timeFormat.format(new Date()));


                        isFirstLoad = false;

                    } catch (Exception e) {
                        System.err.println("Error updating UI: " + e.getMessage());
                        showErrorState();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading statistics: " + e.getMessage());
                Platform.runLater(this::showErrorState);
            }
        });

        loadThread.setDaemon(true);
        loadThread.setName("Statistics-Loader");
        loadThread.start();
    }


    private void animateLabelUpdate(Label label, String newValue) {
        if (label == null) return;


        javafx.animation.ScaleTransition scaleOut = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), label);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(150), label);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);


        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(100), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);

        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(100), label);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);


        scaleOut.setOnFinished(e1 -> {
            fadeOut.play();
            fadeOut.setOnFinished(e2 -> {
                label.setText(newValue);
                fadeIn.play();
                fadeIn.setOnFinished(e3 -> {
                    scaleIn.play();
                });
            });
        });
        scaleOut.play();
    }


    private void updateGenderChart(Map<String, Long> genderStats) {
        if (genderChart == null || genderStats == null) return;


        genderChart.setAnimated(true);
        genderChart.setTitle("Gender Distribution");

        if (genderStats.isEmpty()) {
            // 如果没有数据，显示占位符
            genderChart.getData().clear();
            PieChart.Data noData = new PieChart.Data("No Data Available", 1);
            genderChart.getData().add(noData);
            return;
        }


        genderChart.getData().clear();

        genderStats.forEach((gender, count) -> {
            String label;
            if (gender == null || gender.trim().isEmpty()) {
                label = "Not Specified";
            } else {
                label = gender.substring(0, 1).toUpperCase() +
                        gender.substring(1).toLowerCase();
            }

            PieChart.Data slice = new PieChart.Data(
                    String.format("%s (%d)", label, count),
                    count
            );
            genderChart.getData().add(slice);
        });


        genderChart.layout();
    }


    private void updateCountryChart(Map<String, Long> countryStats) {
        if (countryChart == null || countryStats == null) return;


        countryChart.setAnimated(true);
        countryChart.setTitle("Top Countries");


        CategoryAxis xAxis = (CategoryAxis) countryChart.getXAxis();
        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);
        xAxis.setTickMarkVisible(true);

        var series = new XYChart.Series<String, Number>();
        series.setName("Users");

        if (countryStats.isEmpty()) {

            countryChart.getData().clear();
            series.getData().add(new XYChart.Data<>("No Data Available", 1));
            countryChart.getData().add(series);
        } else {

            countryChart.getData().clear();


            countryStats.entrySet().stream()
                    .filter(e -> e.getKey() != null && !e.getKey().trim().isEmpty())
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .forEach(entry -> {
                        series.getData().add(
                                new XYChart.Data<>(entry.getKey(), entry.getValue())
                        );
                    });

            countryChart.getData().add(series);
        }


        countryChart.getYAxis().setAutoRanging(true);
        countryChart.layout();
    }


    private void showErrorState() {
        if (onlineCount != null) onlineCount.setText("Error");
        if (offlineCount != null) offlineCount.setText("Error");
        if (totalCount != null) totalCount.setText("Error");
        if (messageCount != null) messageCount.setText("Error");
        if (chatCount != null) chatCount.setText("Error");

        if (genderChart != null) {
            genderChart.getData().clear();
            genderChart.setTitle("Error Loading Data");
        }

        if (countryChart != null) {
            countryChart.getData().clear();
            countryChart.setTitle("Error Loading Data");
        }

        if (lastUpdated != null) {
            lastUpdated.setText("Last updated: Error");
        }
    }
}