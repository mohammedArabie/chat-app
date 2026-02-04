package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.common.dto.InvitationDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.jets.chat.common.util.Functions.getInitials;

public class InvitationCardController {
    @FXML
    private Label nameLabel, timeLabel, typeLabel, messageLabel, initialsLabel;

    private InvitationDTO data;
    private ChatViewModel viewModel;

    public void setData(InvitationDTO data, ChatViewModel viewModel) {
        this.data = data;
        this.viewModel = viewModel;
        nameLabel.setText(data.userName());
        initialsLabel.setText(getInitials(data.userName()));
        messageLabel.setText(data.userName() + " sent you an invitation request");
        typeLabel.setText("Wants to connect with you");
        timeLabel.setText(formatTimeAgo(data.createdAt()));
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Just now";
        }
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 7) {
            return days + " days ago";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }

    @FXML
    private void onAccept() throws RemoteException {
        viewModel.acceptInvitation(data);
        viewModel.getPendingInvitations().remove(data);
    }

    @FXML
    private void onDecline() throws RemoteException {
        viewModel.declineInvitation(data);
        viewModel.getPendingInvitations().remove(data);
    }
}
