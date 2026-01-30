package com.jets.chat.client.controller;

import com.jets.chat.client.util.ClientManager;
import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
import com.jets.chat.common.enums.Gender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.util.Base64;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

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

    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button toggleConfirmPasswordBtn;
    @FXML
    private SVGPath confirmEyeIcon;
    private boolean confirmPasswordVisible = false;
    @FXML
    private TextField visibleConfirmPasswordField;

    // Profile picture fields
    @FXML
    private StackPane profilePicturePreview;
    @FXML
    private ImageView profileImageView;
    @FXML
    private ProgressIndicator uploadProgress;
    @FXML
    private Button uploadPictureBtn;
    @FXML
    private Button removePictureBtn;
    @FXML
    private Label fileNameLabel;

    // Store the Base64 string
    private String profilePictureBase64;

    private StringProperty passwordProperty = new SimpleStringProperty("");
    private StringProperty confirmPasswordProperty = new SimpleStringProperty("");

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

        // Bind both fields to the same property
        passwordField.textProperty().bindBidirectional(passwordProperty);
        visiblePasswordField.textProperty().bindBidirectional(passwordProperty);

        confirmPasswordField.textProperty().bindBidirectional(confirmPasswordProperty);
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordProperty);

        // Setup bio character counter
        setupBioCharacterCounter();

        dobPicker.setEditable(false);

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
                "Japan", "China", "India", "Egypt", "Brazil", "Mexico", "South Korea",
                "Netherlands", "Sweden", "Norway", "Denmark", "Finland", "Switzerland", "Austria",
                "Belgium", "Poland", "Portugal", "Ireland", "New Zealand", "Singapore", "Malaysia",
                "Thailand", "Vietnam");
        countryComboBox.setItems(countries);
    }

    private void setupPasswordStrengthListener() {
        passwordProperty.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue != null && !newValue.isEmpty()) {
                    passwordStrengthContainer.setVisible(true);
                    passwordStrengthContainer.setManaged(true);
                    updatePasswordStrength(newValue);
                } else {
                    passwordStrengthContainer.setVisible(false);
                    passwordStrengthContainer.setManaged(false);
                }
            });
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
            if (!icon.getStyleClass().contains("valid")) {
                icon.getStyleClass().add("valid");
            }
            // updating the parent label
            if (icon.getParent() instanceof HBox hbox) {
                hbox.getChildren().stream().filter(node -> node instanceof Label).forEach(node -> {
                    if (!node.getStyleClass().contains("valid")) {
                        node.getStyleClass().add("valid");
                    }
                });
            }
        } else {
            icon.getStyleClass().removeAll("valid");
            if (icon.getParent() instanceof HBox hbox) {
                hbox.getChildren().stream().filter(node -> node instanceof Label)
                        .forEach(node -> node.getStyleClass().removeAll("valid"));
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

            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            eyeIcon.setContent(EYE_CLOSED);
            Platform.runLater(() -> {
                visiblePasswordField.requestFocus();
                updatePasswordStrength(passwordProperty.get());
            });
        } else {

            passwordField.setVisible(true);
            passwordField.setManaged(true);

            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            eyeIcon.setContent(EYE_OPEN);
            Platform.runLater(() -> {
                passwordField.requestFocus();
                updatePasswordStrength(passwordProperty.get());
            });
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;

        if (confirmPasswordVisible) {
            // Show visible field, hide password field
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmEyeIcon.setContent(EYE_CLOSED);

            Platform.runLater(() -> visibleConfirmPasswordField.requestFocus());
        } else {
            // Show password field, hide visible field
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);
            confirmEyeIcon.setContent(EYE_OPEN);

            Platform.runLater(() -> confirmPasswordField.requestFocus());
        }
    }

    @FXML
    private void chooseProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");

        // Set file filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files",
                "*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(imageFilter);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        // Show dialog
        File file = fileChooser.showOpenDialog(uploadPictureBtn.getScene().getWindow());

        if (file != null) {
            // Validate file size (5MB max)
            long fileSize = file.length();
            if (fileSize > 5 * 1024 * 1024) { // 5MB in bytes
                showAlert(Alert.AlertType.ERROR, "File Too Large",
                        "Please select an image smaller than 5MB.");
                return;
            }

            // Show upload progress
            uploadProgress.setVisible(true);

            // Process in background thread to avoid UI freeze
            new Thread(() -> {
                try {
                    // Read file and convert to Base64
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    profilePictureBase64 = Base64.getEncoder().encodeToString(fileBytes);

                    // Load image for preview
                    Image image = new Image(file.toURI().toString(), 80, 80, true, true);

                    Platform.runLater(() -> {
                        // Update UI on JavaFX thread
                        profileImageView.setImage(image);
                        profileImageView.setVisible(true);
                        uploadProgress.setVisible(false);

                        // Show file name
                        fileNameLabel.setText(file.getName());
                        fileNameLabel.setVisible(true);

                        // Show remove button
                        removePictureBtn.setVisible(true);
                    });

                } catch (IOException e) {
                    Platform.runLater(() -> {
                        uploadProgress.setVisible(false);
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Could not load image: " + e.getMessage());
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        uploadProgress.setVisible(false);
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "An unexpected error occurred: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    @FXML
    private void removeProfilePicture() {
        // Reset everything
        profilePictureBase64 = null;
        profileImageView.setImage(null);
        profileImageView.setVisible(false);
        fileNameLabel.setVisible(false);
        removePictureBtn.setVisible(false);
    }

    // NEW METHOD: Update DTO with all fields including profile picture
    private void updateRegistrationDTO(RegisterRequestDTO dto) {
        dto.setDisplayName(fullNameField.getText().trim());
        dto.setEmail(emailField.getText().trim());
        dto.setPhoneNumber(phoneField.getText().trim());
        dto.setPassword(passwordProperty.get());
        dto.setGender(Gender.valueOf(genderComboBox.getValue().toUpperCase()));
        dto.setCountry(countryComboBox.getValue());
        dto.setDateOfBirth(Date.valueOf(dobPicker.getValue()));
        dto.setBio(bioTextArea.getText().trim());

        // Add profile picture if selected (as Base64 string)
        if (profilePictureBase64 != null && !profilePictureBase64.isEmpty()) {
            dto.setProfilePicture(profilePictureBase64);
        }
    }

    @FXML
    private void handleRegister() {
        if (!validateFields())
            return;

        RegisterRequestDTO dto = new RegisterRequestDTO();
        updateRegistrationDTO(dto); // Use the new method that includes profile picture

        new Thread(() -> {
            try {
                RegisterResponseDTO response = ClientManager.getInstance().getRemoteUserService()
                        .register(dto);

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Account created successfully!");
                        // Optional: Clear form or navigate to login
                        // clearForm();
                        // navigateToLogin();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Registration Failed",
                                response.getMessage());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Server Error",
                        "Server is not running or unreachable"));
            }
        }).start();
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        String confirmPassword = confirmPasswordProperty.get();

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
        } else if (!isValidPhone(phoneField.getText().trim())) {
            errors.append("- Phone number must contain only digits (10-15 digits)\n");
        }

        String password = passwordProperty.get();
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

        if (confirmPassword.isEmpty()) {
            errors.append("- Please confirm your password\n");
        } else if (!password.equals(confirmPassword)) {
            errors.append("- Passwords do not match\n");
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

    private boolean isValidPhone(String phone) {
        String phoneRegex = "^[0-9]{10,15}$";
        return phone.matches(phoneRegex);
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

    // Optional: Clear form method
    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordProperty.set("");
        confirmPasswordProperty.set("");
        genderComboBox.getSelectionModel().clearSelection();
        countryComboBox.getSelectionModel().clearSelection();
        dobPicker.setValue(null);
        bioTextArea.clear();
        removeProfilePicture(); // Clear profile picture
    }
}