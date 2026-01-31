package com.jets.chat.server.admin.controller;

import com.jets.chat.common.enums.Gender;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.entity.User;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

        // Gender Column (with proper enum handling)
        TableColumn<User, String> genderCol = (TableColumn<User, String>) usersTable.getColumns().get(5);
        genderCol.setCellValueFactory(cellData -> {
            Gender gender = cellData.getValue().getGender(); // This returns Gender enum
            if (gender == null) {
                return new SimpleStringProperty("N/A");
            }

            // Convert enum to readable string
            String genderString = gender.name();
            if (genderString == null || genderString.isEmpty()) {
                return new SimpleStringProperty("N/A");
            }

            // Format: First letter uppercase, rest lowercase
            String formattedGender = genderString.substring(0, 1).toUpperCase() +
                    genderString.substring(1).toLowerCase();
            return new SimpleStringProperty(formattedGender);
        });

        // Status Column (with badge styling)
        TableColumn<User, String> statusCol = (TableColumn<User, String>) usersTable.getColumns().get(6);
        statusCol.setCellValueFactory(cellData -> {
            // Get actual status from database
            try {
                long userId = cellData.getValue().getUserId();
                // This assumes you have a method in UserDao to get status by user ID
                // If not, you'll need to add it or use an alternative approach
                return new SimpleStringProperty("Offline"); // Placeholder for now
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown");
            }
        });

        statusCol.setCellFactory(col -> new TableCell<User, String>() {
            private final HBox box = new HBox();
            private final Label badge = new Label();

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                badge.getStyleClass().addAll("status-badge", "status-offline");
                box.getChildren().add(badge);
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    // Remove all status classes
                    badge.getStyleClass().removeAll("status-online", "status-offline", "status-busy", "status-away");

                    // Add appropriate status class based on actual status
                    String statusClass = "status-" + status.toLowerCase();
                    badge.getStyleClass().addAll("status-badge", statusClass);
                    badge.setText(status);

                    setGraphic(box);
                }
            }
        });

        // Last Seen Column (formatted date)
        TableColumn<User, Timestamp> lastSeenCol = (TableColumn<User, Timestamp>) usersTable.getColumns().get(7);
        lastSeenCol.setCellValueFactory(cellData -> {
            Timestamp timestamp = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleObjectProperty<>(timestamp);
        });
        lastSeenCol.setCellFactory(col -> new TableCell<User, Timestamp>() {
            @Override
            protected void updateItem(Timestamp timestamp, boolean empty) {
                super.updateItem(timestamp, empty);
                if (empty || timestamp == null) {
                    setText("-");
                } else {
                    setText(dateFormat.format(timestamp));
                }
            }
        });
    }

    @FXML
    public void loadUsers() {
        // Show loading state
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
        totalUsersLabel.setText(String.valueOf(filtered.size()) + " of " + allUsers.size());
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("User Management Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}