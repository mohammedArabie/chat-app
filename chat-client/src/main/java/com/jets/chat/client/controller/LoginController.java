package com.jets.chat.client.controller;

import com.jets.chat.client.callback.ClientCallbackImpl;
import com.jets.chat.client.util.ClientManager;
import com.jets.chat.client.util.SceneManager;
import com.jets.chat.client.util.SessionManager;
import com.jets.chat.common.dto.LoginResult;
import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.rmi.RemoteUserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.rmi.RemoteException;
import java.util.Properties;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private Button signInButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Hyperlink signupLink;
    @FXML
    private SVGPath togglePasswordVisibility;

    private RemoteUserService userService;
    private ClientCallbackImpl clientCallback;
    private boolean isPasswordVisible = false;

    // SVG paths for eye icons
    private static final String EYE_OPEN_PATH = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private static final String EYE_CLOSED_PATH = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

    @FXML
    public void initialize() {
        userService = ClientManager.getInstance().getRemoteUserService();

        // Initialize callback when scene is ready
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) emailField.getScene().getWindow();
                clientCallback = new ClientCallbackImpl(stage);
                System.out.println("ClientCallback initialized");

                // Now that callback is ready, check for auto-login
                checkAutoLogin();
            } catch (RemoteException e) {
                showError("Failed to initialize connection");
                e.printStackTrace();
                signInButton.setDisable(true);
            }
        });

        // Setup password visibility toggle
        setupPasswordToggle();

        signInButton.setOnAction(e -> handleLogin());

        emailField.setOnKeyTyped(e -> errorLabel.setVisible(false));
        passwordField.setOnKeyTyped(e -> errorLabel.setVisible(false));
        passwordTextField.setOnKeyTyped(e -> errorLabel.setVisible(false));
    }

    private void setupPasswordToggle() {
        if (togglePasswordVisibility != null) {
            togglePasswordVisibility.setOnMouseClicked(event -> togglePasswordVisibility());
        }
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            togglePasswordVisibility.setContent(EYE_CLOSED_PATH);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            togglePasswordVisibility.setContent(EYE_OPEN_PATH);
        }
    }

    private void handleLogin() {
        String input = emailField.getText().trim();
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();

        if (input.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        signInButton.setDisable(true);

        new Thread(() -> {
            try {
                LoginResult result = userService.login(input, password, clientCallback);

                Platform.runLater(() -> {
                    signInButton.setDisable(false);

                    if (result.isSuccess()) {
                        System.out.println("Login Successful");
                        if (rememberMeCheckbox.isSelected()) {
                            SessionManager.saveSession(result.getUserDto().getId(),
                                    result.getSessionId());
                        }
                        navigateToMainScreen(result.getUserDto());
                    } else {
                        showError(result.getErrorMessage());
                    }
                });

            } catch (RemoteException ex) {
                Platform.runLater(() -> {
                    signInButton.setDisable(false);
                    showError("Connection error: " + ex.getMessage());
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void checkAutoLogin() {
        Properties props = SessionManager.loadSession();

        if (props != null) {
            String userIdStr = props.getProperty("userId");
            String sessionId = props.getProperty("sessionId");

            if (userIdStr != null && sessionId != null) {
                try {
                    long userId = Long.parseLong(userIdStr);

                    signInButton.setDisable(true);
                    if (errorLabel != null)
                        errorLabel.setText("Restoring session...");

                    new Thread(() -> {
                        try {
                            UserDTO userDto = userService.reconnect(userId, sessionId,
                                    clientCallback);

                            Platform.runLater(() -> {
                                signInButton.setDisable(false);

                                if (userDto != null) {
                                    System.out.println("Auto-login successful for: "
                                            + userDto.getDisplayName());
                                    navigateToMainScreen(userDto);
                                } else {
                                    System.out.println(
                                            "Saved session is invalid. Clearing local data.");
                                    SessionManager.clearSession();
                                    showError("Session expired. Please sign in again.");
                                }
                            });

                        } catch (RemoteException e) {
                            Platform.runLater(() -> {
                                signInButton.setDisable(false);
                                showError("Connection lost: Unable to reach server.");
                            });
                            e.printStackTrace();
                        }
                    }).start();

                } catch (NumberFormatException e) {
                    SessionManager.clearSession();
                }
            }
        }
    }

    private void navigateToMainScreen(UserDTO user) {
        System.out.println("Navigate to main screen for user: " + user.getDisplayName());
        SceneManager.getInstance().showMainScreen(user.getDisplayName());
    }

    @FXML
    private void navigateToRegister() {
        System.out.println("Navigate to register screen");
        SceneManager.getInstance().showRegisterScreen();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}