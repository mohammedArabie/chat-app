package com.jets.chat.client.util;

import com.jets.chat.common.dto.AnnouncementDTO;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class SystemNotificationUtil {

    public static void showNotification(String title, String message, NotificationType type) {
        Platform.runLater(() -> {
            Notifications notificationBuilder = Notifications.create().title(title).text(message)
                    .hideAfter(Duration.seconds(5)).position(Pos.BOTTOM_RIGHT)
                    .threshold(3, Notifications.create().title("Too many messages"))
                    .owner(SceneManager.getInstance().getPrimaryStage());;

            switch (type) {
                case INFO -> notificationBuilder.showInformation();
                case WARNING -> notificationBuilder.showWarning();
                case ERROR -> notificationBuilder.showError();
                case SUCCESS -> notificationBuilder.showConfirm();
            }
        });
    }

    public static void showInfoNotification(String title, String message) {
        showNotification(title, message, NotificationType.INFO);
    }

    public static void showWarningNotification(String title, String message) {
        showNotification(title, message, NotificationType.WARNING);
    }

    public enum NotificationType {
        INFO, WARNING, ERROR, SUCCESS
    }

    public static void showAnnouncement(AnnouncementDTO announcement) {
        Platform.runLater(() -> {
            Label titleLabel = new Label("📢 Server Announcement");
            titleLabel
                    .setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

            Label messageLabel = new Label(announcement.getContent());
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(350);
            StringBuilder style = new StringBuilder();

            style.append("-fx-font-size: 13px; ");

            if (announcement.getFontStyle() != null && !announcement.getFontStyle().isEmpty()) {
                style.append("-fx-font-family: '").append(announcement.getFontStyle())
                        .append("'; ");
            }

            if (announcement.getFontColor() != null && !announcement.getFontColor().isEmpty()) {
                style.append("-fx-text-fill: ").append(announcement.getFontColor()).append("; ");
            } else {
                style.append("-fx-text-fill: #E0E0E0; ");
            }

            if (announcement.isBold()) {
                style.append("-fx-font-weight: bold; ");
            }

            if (announcement.isItalic()) {
                style.append("-fx-font-style: italic; ");
            }

            messageLabel.setStyle(style.toString());

            VBox container = new VBox(5, titleLabel, messageLabel);

            Notifications.create().graphic(container).position(Pos.BOTTOM_RIGHT)
                    .hideAfter(Duration.seconds(10))
                    .owner(SceneManager.getInstance().getPrimaryStage()).show();
        });
    }
}