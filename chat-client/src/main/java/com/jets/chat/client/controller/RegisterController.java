package com.jets.chat.client.controller;

import com.jets.chat.client.util.ClientManager;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.enums.Gender;

import java.sql.Date;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button togglePasswordBtn;
    @FXML
    private SVGPath eyeIcon;
    @FXML
    private HBox passwordStrengthContainer;
    @FXML
    private SVGPath checkMinLength;
    @FXML
    private SVGPath checkUppercase;
    @FXML
    private SVGPath checkNumber;
    @FXML
    private SVGPath checkSpecial;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private ComboBox<String> countryComboBox;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextArea bioTextArea;
    @FXML
    private Label bioCharCount;
    @FXML
    private Button registerBtn;
    @FXML
    private Hyperlink signinLink;

    private boolean passwordVisible = false;
    @FXML
    private TextField visiblePasswordField;

    // Eye icons SVG paths
    private static final String EYE_OPEN = "M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8zM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";
    private static final String EYE_CLOSED = "M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24M1 1l22 22";

    private static final int MAX_BIO_LENGTH = 200;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize country list
        initializeCountries();

        // Setup password strength listener
        setupPasswordStrengthListener();

        // Setup bio character counter
        setupBioCharacterCounter();

        // Set date picker constraints
        dobPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now().minusYears(13)));
            }
        });
    }

    private void initializeCountries() {
        ObservableList<String> countries = FXCollections.observableArrayList("United States",
                "United Kingdom", "Canada", "Australia", "Germany", "France", "Spain", "Italy",
                "Japan", "China", "India", "Egypt", "Brazil", "Mexico", "South Korea", "Netherlands",
                "Sweden", "Norway", "Denmark", "Finland", "Switzerland", "Austria", "Belgium",
                "Poland", "Portugal", "Ireland", "New Zealand", "Singapore", "Malaysia", "Thailand",
                "Vietnam");
        countryComboBox.setItems(countries);
    }

    private void setupPasswordStrengthListener() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                passwordStrengthContainer.setVisible(true);
                passwordStrengthContainer.setManaged(true);
                updatePasswordStrength(newValue);
            } else {
                passwordStrengthContainer.setVisible(false);
                passwordStrengthContainer.setManaged(false);
            }
        });
    }

    private void updatePasswordStrength(String password) {
        // Check minimum length (8+ characters)
        boolean hasMinLength = password.length() >= 8;
        updateStrengthIndicator(checkMinLength, hasMinLength);

        // Check uppercase letter
        boolean hasUppercase = password.matches(".*[A-Z].*");
        updateStrengthIndicator(checkUppercase, hasUppercase);

        // Check number
        boolean hasNumber = password.matches(".*[0-9].*");
        updateStrengthIndicator(checkNumber, hasNumber);

        // Check special character
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");
        updateStrengthIndicator(checkSpecial, hasSpecial);
    }

    private void updateStrengthIndicator(SVGPath icon, boolean isValid) {
        if (isValid) {
            icon.getStyleClass().add("valid");
            // Also update the parent label if needed
            if (icon.getParent() instanceof HBox hbox) {
                hbox.getChildren().stream().filter(node -> node instanceof Label)
                        .forEach(node -> node.getStyleClass().add("valid"));
            }
        } else {
            icon.getStyleClass().remove("valid");
            if (icon.getParent() instanceof HBox hbox) {
                hbox.getChildren().stream().filter(node -> node instanceof Label)
                        .forEach(node -> node.getStyleClass().remove("valid"));
            }
        }
    }

    private void setupBioCharacterCounter() {
        bioTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.length() > MAX_BIO_LENGTH) {
                    bioTextArea.setText(oldValue);
                } else {
                    bioCharCount.setText(newValue.length() + "/" + MAX_BIO_LENGTH);
                }
            }
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());

            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            eyeIcon.setContent(EYE_CLOSED);
        } else {
            passwordField.setText(visiblePasswordField.getText());

            passwordField.setVisible(true);
            passwordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            eyeIcon.setContent(EYE_OPEN);
        }
    }


    @FXML
    private void handleRegister() {

        if (!validateFields())
            return;

        try {
            RegisterRequestDTO dto = new RegisterRequestDTO();

            dto.setDisplayName(fullNameField.getText().trim());
            dto.setEmail(emailField.getText().trim());
            dto.setPhoneNumber(phoneField.getText().trim());
            dto.setPassword(passwordField.getText());

            dto.setGender(Gender.valueOf(genderComboBox.getValue()));

            dto.setCountry(countryComboBox.getValue());
            dto.setDateOfBirth(Date.valueOf(dobPicker.getValue()));
            dto.setBio(bioTextArea.getText().trim());

            RegisterResponseDTO response = ClientManager.getInstance().getRemoteUserService()
                    .register(dto);

            if (response.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Account created successfully 🎉");
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", response.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Server Error", "Could not connect to server");
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (fullNameField.getText().trim().isEmpty()) {
            errors.append("- Full name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("- Email is required\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("- Please enter a valid email address\n");
        }

        if (phoneField.getText().trim().isEmpty()) {
            errors.append("- Phone number is required\n");
        }

        String password = passwordField.getText();
        if (password.isEmpty()) {
            errors.append("- Password is required\n");
        } else {
            if (password.length() < 8) {
                errors.append("- Password must be at least 8 characters\n");
            }
            if (!password.matches(".*[A-Z].*")) {
                errors.append("- Password must contain an uppercase letter\n");
            }
            if (!password.matches(".*[0-9].*")) {
                errors.append("- Password must contain a number\n");
            }
            if (!password.matches(".*[!@#$%^&*].*")) {
                errors.append("- Password must contain a special character\n");
            }
        }

        if (genderComboBox.getValue() == null) {
            errors.append("- Please select your gender\n");
        }

        if (countryComboBox.getValue() == null) {
            errors.append("- Please select your country\n");
        }

        if (dobPicker.getValue() == null) {
            errors.append("- Date of birth is required\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    @FXML
    private void navigateToLogin() {
        // TODO: Implement navigation to login page
        System.out.println("Navigate to login page");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #fafafa;");

        alert.showAndWait();
    }
}
