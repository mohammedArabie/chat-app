package com.jets.chat.client.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ResourceBundle;

public class ProfileAvatarController {
    @FXML
    private Button uploadImageBtn, editNameBtn;
    @FXML
    private TextField nameField;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Text avatarText;
    @FXML
    private StackPane imageContainer;
    @FXML
    private ResourceBundle resources;

    private ProfileController mainController;
    private Image currentProfileImage;

    @FXML
    public void initialize() {
        setupNameField();
        setupImageClipping();
        imageContainer.setVisible(false);
        imageContainer.setManaged(false);

        nameField.textProperty().addListener((o, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 50)
                nameField.setText(oldVal);
        });
    }

    public void setMainController(ProfileController mainController) {
        this.mainController = mainController;
    }

    private void setupNameField() {
        nameField.setEditable(false);
        nameField.setOnAction(e -> {
            commitNameChange();
            imageContainer.requestFocus();
        });
        nameField.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv && nameField.isEditable())
                commitNameChange();
        });
    }

    private void setupImageClipping() {
        Circle clip = new Circle(60, 60, 60);
        profileImageView.setClip(clip);
    }

    private void commitNameChange() {
        nameField.setEditable(false);
        nameField.getStyleClass().remove("name-field-editing");
        nameField.getStyleClass().add("name-field-readonly");
        if (mainController != null) {
            mainController.saveProfileNameOnly(nameField.getText());
        }
    }

    @FXML
    private void handleEditName() {
        nameField.setEditable(true);
        nameField.getStyleClass().removeAll("name-field-readonly");
        nameField.getStyleClass().add("name-field-editing");
        nameField.requestFocus();
        nameField.selectAll();
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Profile Image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg",
                "*.jpeg", "*.png", "*.gif"));
        File file = fc.showOpenDialog(uploadImageBtn.getScene().getWindow());

        if (file != null) {
            String imagePath = file.getAbsolutePath();
            Task<Image> imageLoadTask = new Task<>() {
                @Override
                protected Image call() {
                    return new Image(file.toURI().toString());
                }
            };
            imageLoadTask.setOnSucceeded(e -> {
                Image img = imageLoadTask.getValue();
                if (img != null && !img.isError()) {
                    setImage(img);
                    if (mainController != null) {
                        mainController.saveProfilePicturePathOnly(imagePath);
                    }
                }
            });
            new Thread(imageLoadTask).start();
        }
    }

    public String getName() {
        return nameField.getText();
    }
    public void setName(String n) {
        nameField.setText(n);
    }
    public Image getImage() {
        return currentProfileImage;
    }

    public void setImage(Image i) {
        currentProfileImage = i;
        if (i != null) {
            profileImageView.setImage(i);
            imageContainer.setVisible(true);
            imageContainer.setManaged(true);
            avatarText.setVisible(false);
            avatarText.setManaged(false);
        } else {
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
            avatarText.setVisible(true);
            avatarText.setManaged(true);
        }
    }
}