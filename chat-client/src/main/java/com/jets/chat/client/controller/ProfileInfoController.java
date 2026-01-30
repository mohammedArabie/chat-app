package com.jets.chat.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfileInfoController {
    @FXML
    private Text emailText, phoneText;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField, countryField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextArea bioArea;
    @FXML
    private Button saveButton, editButton;
    @FXML
    private SVGPath eyeIcon, editIcon;
    @FXML
    private ResourceBundle resources;

    private ProfileController mainController;
    private boolean editMode = false;
    private boolean isPasswordVisible = false;

    private static final String PENCIL_ICON = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private static final String CLOSE_ICON = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";
    private static final String EYE_OPEN = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private static final String EYE_CLOSED = "M2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27z";

    @FXML
    public void initialize() {
        if (resources != null) {
            genderComboBox.getItems().addAll(resources.getString("gender.male"),
                    resources.getString("gender.female"));
        }
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        setFieldsDisabled(true);
    }

    public void setMainController(ProfileController main) {
        this.mainController = main;
    }

    @FXML
    private void handleEdit() {
        editMode = !editMode;
        setFieldsDisabled(!editMode);

        if (editMode) {
            editIcon.setContent(CLOSE_ICON);
            editIcon.setFill(Color.web("#0D9FFF"));
            passwordField.requestFocus();
            passwordField.selectAll();
        } else {
            editIcon.setContent(PENCIL_ICON);
            editIcon.setFill(Color.web("#0D9FFF"));
            mainController.restoreData();
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        eyeIcon.setContent(isPasswordVisible ? EYE_CLOSED : EYE_OPEN);
        passwordField.setVisible(!isPasswordVisible);
        passwordField.setManaged(!isPasswordVisible);
        passwordTextField.setVisible(isPasswordVisible);
        passwordTextField.setManaged(isPasswordVisible);
    }

    @FXML
    private void handleSave() {
        mainController.saveProfile();
    }

    public void setGender(String g) {
        if (g == null || g.isEmpty()) {
            genderComboBox.getSelectionModel().clearSelection();
            return;
        }

        for (String item : genderComboBox.getItems()) {
            if (item.equalsIgnoreCase(g)) {
                genderComboBox.setValue(item);
                return;
            }
        }
        genderComboBox.setValue(g);
    }

    public void setEmail(String e) {
        if (e != null)
            emailText.setText(e);
    }
    public void setPhone(String p) {
        if (p != null)
            phoneText.setText(p);
    }
    public void setPassword(String p) {
        passwordField.setText(p);
    }
    public void setCountry(String c) {
        countryField.setText(c);
    }
    public void setDob(LocalDate d) {
        dobPicker.setValue(d);
    }
    public void setBio(String b) {
        bioArea.setText(b);
    }

    public String getPassword() {
        return passwordField.getText();
    }
    public String getCountry() {
        return countryField.getText();
    }
    public LocalDate getDob() {
        return dobPicker.getValue();
    }
    public String getBio() {
        return bioArea.getText();
    }
    public String getGender() {
        return genderComboBox.getValue();
    }

    private void setFieldsDisabled(boolean b) {
        passwordField.setDisable(b);
        passwordTextField.setDisable(b);
        genderComboBox.setDisable(b);
        countryField.setDisable(b);
        dobPicker.setDisable(b);
        bioArea.setDisable(b);
        saveButton.setDisable(b);
    }

    public void resetEditMode() {
        editMode = false;
        editIcon.setContent(PENCIL_ICON);
        editIcon.setFill(Color.web("#0D9FFF"));
        setFieldsDisabled(true);
    }
}