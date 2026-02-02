package com.jets.chat.server.admin.controller;

import com.jets.chat.common.enums.Gender;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.entity.User;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class UserManagementController {
    @FXML private TableView<User> usersTable;
    @FXML private TextField searchField;
    @FXML private Label totalUsersLabel;

    private ServerManager serverManager;
    private UserDao userDao;
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public void init(ServerManager serverManager) {
        this.serverManager = serverManager;

        try {
            HikariDataSource dataSource = com.jets.chat.server.config.DataSourceConfig.getDataSource();
            this.userDao = new UserDaoImpl(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to initialize user data access");
            return;
        }

        setupTable();
        loadUsers();
    }

    private void setupTable() {
        // ID Column
        TableColumn<User, Long> idCol = (TableColumn<User, Long>) usersTable.getColumns().get(0);
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        // Display Name Column
        TableColumn<User, String> nameCol = (TableColumn<User, String>) usersTable.getColumns().get(1);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));

        // Phone Column
        TableColumn<User, String> phoneCol = (TableColumn<User, String>) usersTable.getColumns().get(2);
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        // Email Column
        TableColumn<User, String> emailCol = (TableColumn<User, String>) usersTable.getColumns().get(3);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Country Column
        TableColumn<User, String> countryCol = (TableColumn<User, String>) usersTable.getColumns().get(4);
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));

        // Gender Column (proper enum handling)
        TableColumn<User, String> genderCol = (TableColumn<User, String>) usersTable.getColumns().get(5);
        genderCol.setCellValueFactory(cellData -> {
            Gender gender = cellData.getValue().getGender();
            if (gender == null) return new SimpleStringProperty("N/A");

            String genderStr = gender.name();
            return new SimpleStringProperty(
                    genderStr.substring(0, 1).toUpperCase() +
                            genderStr.substring(1).toLowerCase()
            );
        });

        TableColumn<User, String> statusCol = (TableColumn<User, String>) usersTable.getColumns().get(6);
        statusCol.setCellValueFactory(cellData -> {
            long userId = cellData.getValue().getUserId();
            UserStatus status = userDao.getStatus(userId); // Returns AVAILABLE/OFFLINE/BUSY/AWAY

            // Convert enum to display-friendly string (AVAILABLE → "Available")
            String displayStatus = status.name().substring(0, 1).toUpperCase() +
                    status.name().substring(1).toLowerCase();
            return new SimpleStringProperty(displayStatus);
        });

// Status badge styling with CORRECT ENUM → CSS MAPPING
        statusCol.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox();
            private final Label badge = new Label();

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                box.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));
                badge.getStyleClass().add("status-badge"); // Uses YOUR existing CSS class
                box.getChildren().add(badge);
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }

                badge.setText(status);
                badge.getStyleClass().removeAll("status-online", "status-offline", "status-busy", "status-away");

                // ✅ CRITICAL FIX: Map Java enum values to YOUR CSS classes
                switch (status.toLowerCase()) {
                    case "available" -> badge.getStyleClass().add("status-online");   // AVAILABLE → Green
                    case "offline" -> badge.getStyleClass().add("status-offline");     // OFFLINE → Red
                    case "busy" -> badge.getStyleClass().add("status-busy");           // BUSY → Orange
                    case "away" -> badge.getStyleClass().add("status-away");           // AWAY → Yellow/Gray
                    default -> badge.getStyleClass().add("status-offline");
                }

                setGraphic(box);
            }
        });

        // Last Seen Column (using created_at as fallback - can be improved later)
        TableColumn<User, Timestamp> lastSeenCol = (TableColumn<User, Timestamp>) usersTable.getColumns().get(7);
        lastSeenCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCreatedAt())
        );
        lastSeenCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp timestamp, boolean empty) {
                super.updateItem(timestamp, empty);
                setText(empty || timestamp == null ? "-" : dateFormat.format(timestamp));
            }
        });
    }

    @FXML
    public void loadUsers() {
        totalUsersLabel.setText("Loading...");

        new Thread(() -> {
            try {
                List<User> users = userDao.getAllUsers();
                Platform.runLater(() -> {
                    allUsers.setAll(users);
                    usersTable.setItems(allUsers);
                    totalUsersLabel.setText(String.valueOf(users.size()));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Failed to load users: " + e.getMessage());
                    totalUsersLabel.setText("Error");
                });
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            usersTable.setItems(allUsers);
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
            return;
        }

        ObservableList<User> filtered = allUsers.stream()
                .filter(user ->
                        user.getDisplayName().toLowerCase().contains(query) ||
                                user.getPhoneNumber().toLowerCase().contains(query) ||
                                (user.getEmail() != null && user.getEmail().toLowerCase().contains(query)) ||
                                (user.getCountry() != null && user.getCountry().toLowerCase().contains(query)) ||
                                (user.getGender() != null && user.getGender().name().toLowerCase().contains(query))
                )
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        usersTable.setItems(filtered);
        totalUsersLabel.setText(filtered.size() + " of " + allUsers.size());
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("User Management Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}