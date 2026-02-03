package com.jets.chat.client.callback;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.rmi.AnnouncementCallback;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientAnnouncementCallback extends UnicastRemoteObject
        implements
            AnnouncementCallback {

    private final Stage primaryStage;

    public ClientAnnouncementCallback(Stage primaryStage) throws RemoteException {
        this.primaryStage = primaryStage;
    }

    @Override
    public void onAnnouncementReceived(AnnouncementDTO announcement) throws RemoteException {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Server Announcement");
            alert.setHeaderText(null);
            alert.setContentText(announcement.getContent());
            alert.initOwner(primaryStage);
            alert.show();
        });
    }
}