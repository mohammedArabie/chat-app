package com.jets.chat.client.callback;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.dto.AnnouncementDTO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {
    private final Stage primaryStage;

    public ClientCallbackImpl(Stage primaryStage) throws RemoteException {
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
