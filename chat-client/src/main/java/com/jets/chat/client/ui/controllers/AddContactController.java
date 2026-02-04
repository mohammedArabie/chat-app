package com.jets.chat.client.ui.controllers;

import com.jets.chat.client.ui.viewmodel.ChatViewModel;
import com.jets.chat.client.ui.viewmodel.RightPaneView;
import com.jets.chat.common.dto.UserDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.rmi.RemoteException;

import static com.jets.chat.common.util.Functions.getInitials;

public class AddContactController {
    @FXML
    private TextField searchField;

    @FXML
    private VBox emptyStateBox;
    @FXML
    private VBox userResultBox;
    @FXML
    private VBox notFoundBox;

    @FXML
    private Label resultName;
    @FXML
    private Label resultEmail;
    @FXML
    private Label resultInitials;

    private ChatViewModel viewModel;
    private UserDTO foundUser;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    private void onSearch() throws RemoteException {
        String query = searchField.getText();
        if (query == null || query.isBlank())
            return;

        UserDTO userOpt = viewModel.search(query);

        if (userOpt != null) {
            this.foundUser = userOpt;
            displayResult(foundUser);
        } else {
            displayNotFound();
        }
    }

    private void displayResult(UserDTO user) {
        resultName.setText(user.getDisplayName());
        resultEmail.setText(user.getEmail());
        resultInitials.setText(getInitials(user.getDisplayName()));

        emptyStateBox.setVisible(false);
        emptyStateBox.setManaged(false);
        notFoundBox.setVisible(false);
        notFoundBox.setManaged(false);

        userResultBox.setVisible(true);
        userResultBox.setManaged(true);
    }

    private void displayNotFound() {
        emptyStateBox.setVisible(false);
        emptyStateBox.setManaged(false);
        userResultBox.setVisible(false);
        userResultBox.setManaged(false);

        notFoundBox.setVisible(true);
        notFoundBox.setManaged(true);
    }

    @FXML
    private void onSendInvitation() throws RemoteException {
        if (foundUser != null) {
            viewModel.sendInvitation(foundUser.getId());
            onClose();
        }
    }

    @FXML
    private void onClose() {
        viewModel.activeRightViewProperty().set(RightPaneView.NONE);
    }
}