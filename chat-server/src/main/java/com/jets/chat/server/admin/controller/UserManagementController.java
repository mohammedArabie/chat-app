package com.jets.chat.server.admin.controller;

import com.jets.chat.common.callback.ClientCallback;
import com.jets.chat.common.enums.Gender;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.dao.UserDao;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.entity.User;
import com.jets.chat.server.service.UserService;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

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
    private UserService userService;
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
            Locale.getDefault());

    // Gender converter
    private final StringConverter<Gender> genderConverter = new StringConverter<>() {
        @Override
        public String toString(Gender gender) {
            if (gender == null)
                return "";
            return gender.name().charAt(0) + gender.name().substring(1).toLowerCase();
        }

        @Override
        public Gender fromString(String string) {
            try {
                return Gender.valueOf(string.toUpperCase());
            } catch (Exception e) {
                return Gender.MALE;
            }
        }
    };

    // ✅ ALL COUNTRIES FOR EDITING
    private final ObservableList<String> allCountries = FXCollections.observableArrayList(
            "United States", "United Kingdom", "Canada", "Australia", "Germany", "France", "Spain",
            "Italy", "Japan", "China", "India", "Egypt", "Brazil", "Mexico", "South Korea",
            "Netherlands", "Sweden", "Norway", "Denmark", "Finland", "Switzerland", "Austria",
            "Belgium", "Poland", "Portugal", "Ireland", "New Zealand", "Singapore", "Malaysia",
            "Thailand", "Vietnam");

    // Country converter for table editing
    private final StringConverter<String> countryConverter = new StringConverter<>() {
        @Override
        public String toString(String country) {
            return country == null ? "" : country;
        }

        @Override
        public String fromString(String string) {
            return string == null ? "" : string.trim();
        }
    };

    public void init(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.userService = serverManager.getUserService();

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
        usersTable.getColumns().clear();
        usersTable.setEditable(true);

        // ID Column
        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(70);
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        // Display Name Column
        TableColumn<User, String> nameCol = new TableColumn<>("Display Name");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String newValue = event.getNewValue().trim();
            if (newValue.isEmpty()) {
                showError("Display name cannot be empty");
                usersTable.refresh();
                return;
            }
            if (!newValue.equals(user.getDisplayName())) {
                user.setDisplayName(newValue);
                saveUserUpdate(user);
            }
        });

        // Phone Column
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setPrefWidth(140);
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setCellFactory(TextFieldTableCell.forTableColumn());
        phoneCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String oldPhone = user.getPhoneNumber();
            String newValue = event.getNewValue().trim();

            if (!newValue.matches("^\\d{10,15}$")) {
                showError("Phone must contain 10-15 digits");
                usersTable.refresh();
                return;
            }

            if (!newValue.equals(oldPhone)) {
                if (userService.phoneNumberExists(newValue)) {
                    showError("Phone number already exists in the system");
                    usersTable.refresh();
                    return;
                }

                user.setPhoneNumber(newValue);
                saveUserUpdate(user);
            }
        });

        // Email Column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setPrefWidth(200);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        emailCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String newValue = event.getNewValue().trim();
            if (!newValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showError("Invalid email format");
                usersTable.refresh();
                return;
            }
            if (!newValue.equals(user.getEmail())) {
                user.setEmail(newValue);
                saveUserUpdate(user);
            }
        });

        // ✅ Country Column (Editable with ComboBox - ALL COUNTRIES)
        TableColumn<User, String> countryCol = new TableColumn<>("Country");
        countryCol.setPrefWidth(120);
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));

        // Set cell factory with ComboBox containing all countries
        countryCol.setCellFactory(ComboBoxTableCell.forTableColumn(countryConverter, allCountries));

        countryCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String newValue = event.getNewValue();
            if (newValue != null && !newValue.trim().isEmpty()) {
                String country = newValue.trim();
                if (!country.equals(user.getCountry())) {
                    user.setCountry(country);
                    saveUserUpdate(user);
                }
            } else {
                showError("Country cannot be empty");
                usersTable.refresh();
            }
        });

        // Gender Column
        TableColumn<User, Gender> genderCol = new TableColumn<>("Gender");
        genderCol.setPrefWidth(90);
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol
                .setCellFactory(ComboBoxTableCell.forTableColumn(genderConverter, Gender.values()));
        genderCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            Gender newValue = event.getNewValue();
            if (newValue != null && newValue != user.getGender()) {
                user.setGender(newValue);
                saveUserUpdate(user);
            }
        });

        // Status Column
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(130);
        statusCol.setCellValueFactory(cellData -> {
            long userId = cellData.getValue().getUserId();
            Map<Long, ClientCallback> onlineClients = serverManager.getOnlineClients();
            boolean isOnline = onlineClients.containsKey(userId);
            return new SimpleStringProperty(isOnline ? "Online" : "Offline");
        });

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
                badge.getStyleClass().add(
                        status.equalsIgnoreCase("online") ? "status-online" : "status-offline");
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

        // Double-click to edit
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    usersTable.edit(row.getIndex(), nameCol);
                }
            });
            return row;
        });

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

                    int totalUsers = users.size();
                    totalUsersLabel.setText(String.valueOf(totalUsers));

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

    private void saveUserUpdate(User user) {
        new Thread(() -> {
            try {
                boolean success = userDao.updateUser(user);
                Platform.runLater(() -> {
                    if (success) {
                        showSuccess("User updated successfully!");
                        usersTable.refresh();
                    } else {
                        showError("Failed to update user");
                        usersTable.refresh();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Error updating user: " + e.getMessage());
                    usersTable.refresh();
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

        totalUsersLabel.setText(filteredCount == totalCount
                ? String.valueOf(totalCount)
                : filteredCount + " of " + totalCount);

        Map<Long, ClientCallback> onlineClients = serverManager.getOnlineClients();
        int onlineCount = (int) users.stream()
                .filter(user -> onlineClients.containsKey(user.getUserId())).count();
        onlineUsersLabel.setText(String.valueOf(onlineCount));
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}