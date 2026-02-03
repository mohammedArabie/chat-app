package com.jets.chat.server.admin.controller;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.enums.Gender;
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
import java.util.Map;
import java.util.stream.Collectors;

public class UserManagementController {
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label onlineUsersLabel;

    private ServerManager serverManager;
    private UserDao userDao;
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
            Locale.getDefault());

    public void init(ServerManager serverManager) {
        this.serverManager = serverManager;

        try {
            HikariDataSource dataSource = com.jets.chat.server.config.DataSourceConfig
                    .getDataSource();
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
        // Clear existing columns if any
        usersTable.getColumns().clear();

        // ID Column
        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(70);
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        // Display Name Column
        TableColumn<User, String> nameCol = new TableColumn<>("Display Name");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));

        // Phone Column
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setPrefWidth(140);
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        // Email Column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setPrefWidth(200);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Country Column
        TableColumn<User, String> countryCol = new TableColumn<>("Country");
        countryCol.setPrefWidth(120);
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));

        // Gender Column
        TableColumn<User, String> genderCol = new TableColumn<>("Gender");
        genderCol.setPrefWidth(90);
        genderCol.setCellValueFactory(cellData -> {
            Gender gender = cellData.getValue().getGender();
            if (gender == null)
                return new SimpleStringProperty("N/A");
            String genderStr = gender.name();
            return new SimpleStringProperty(
                    genderStr.substring(0, 1).toUpperCase() + genderStr.substring(1).toLowerCase());
        });

        // Status Column
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(130);
        statusCol.setCellValueFactory(cellData -> {
            long userId = cellData.getValue().getUserId();
            Map<Long, ClientCallback> onlineClients = serverManager.getOnlineClients();
            boolean isOnline = onlineClients.containsKey(userId);
            String status = isOnline ? "Available" : "Offline";
            return new SimpleStringProperty(status);
        });

        // Status badge styling
        statusCol.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox();
            private final Label badge = new Label();

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                box.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));
                badge.getStyleClass().add("status-badge");
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
                badge.getStyleClass().removeAll("status-online", "status-offline");

                if (status.equalsIgnoreCase("available")) {
                    badge.getStyleClass().add("status-online");
                    badge.setText("Online");
                } else {
                    badge.getStyleClass().add("status-offline");
                }

                setGraphic(box);
            }
        });

        // Last Seen Column
        TableColumn<User, String> lastSeenCol = new TableColumn<>("Last Seen");
        lastSeenCol.setPrefWidth(150);
        lastSeenCol.setCellValueFactory(cellData -> {
            Timestamp timestamp = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(timestamp == null ? "-" : dateFormat.format(timestamp));
        });

        // Add all columns to table
        usersTable.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, countryCol, genderCol,
                statusCol, lastSeenCol);
    }

    @FXML
    public void loadUsers() {
        totalUsersLabel.setText("Loading...");
        onlineUsersLabel.setText("...");

        new Thread(() -> {
            try {
                List<User> users = userDao.getAllUsers();
                Platform.runLater(() -> {
                    allUsers.setAll(users);
                    usersTable.setItems(allUsers);

                    // Update counters
                    int totalUsers = users.size();
                    totalUsersLabel.setText(String.valueOf(totalUsers));

                    // Count online users
                    Map<Long, ClientCallback> onlineClients = serverManager.getOnlineClients();
                    int onlineCount = (int) users.stream()
                            .filter(user -> onlineClients.containsKey(user.getUserId())).count();
                    onlineUsersLabel.setText(String.valueOf(onlineCount));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Failed to load users: " + e.getMessage());
                    totalUsersLabel.setText("Error");
                    onlineUsersLabel.setText("Error");
                });
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            usersTable.setItems(allUsers);
            updateCounters(allUsers);
            return;
        }

        ObservableList<User> filtered = allUsers.stream().filter(user -> user.getDisplayName()
                .toLowerCase().contains(query)
                || user.getPhoneNumber().toLowerCase().contains(query)
                || (user.getEmail() != null && user.getEmail().toLowerCase().contains(query))
                || (user.getCountry() != null && user.getCountry().toLowerCase().contains(query))
                || (user.getGender() != null
                        && user.getGender().name().toLowerCase().contains(query)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        usersTable.setItems(filtered);
        updateCounters(filtered);
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private void updateCounters(ObservableList<User> users) {
        int filteredCount = users.size();
        int totalCount = allUsers.size();

        if (filteredCount == totalCount) {
            totalUsersLabel.setText(String.valueOf(totalCount));
        } else {
            totalUsersLabel.setText(filteredCount + " of " + totalCount);
        }

        Map<Long, ClientCallback> onlineClients = serverManager.getOnlineClients();
        int onlineCount = (int) users.stream()
                .filter(user -> onlineClients.containsKey(user.getUserId())).count();
        onlineUsersLabel.setText(String.valueOf(onlineCount));
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("User Management Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}