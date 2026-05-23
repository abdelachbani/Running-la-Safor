package controllers;

import utils.AlertUtils;
import utils.AvatarUtils;
import utils.NavigationTarget;
import utils.NavigationUtils;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class EditProfileController implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private ImageView headerAvatarImageView;
    @FXML private ImageView avatarImageView;
    @FXML private TextField nicknameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker datePicker;
    @FXML private Button logoutButton;
    @FXML private Button backButton;
    @FXML private Button changeAvatarButton;
    @FXML private Button saveButton;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label dateError;
    @FXML private Tooltip mailErrorTooltip;
    @FXML private Tooltip passwordErrorTooltip;
    @FXML private Tooltip ageErrorTooltip;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private String newAvatarPath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadUserData();
        applyStyles();
        setupValidation();
    }

    private void loadUserData() {
        User currentUser = app.getCurrentUser();
        if (currentUser == null) {
            usernameLabel.setText("Usuario");
            return;
        }

        usernameLabel.setText(currentUser.getNickName());
        nicknameField.setText(currentUser.getNickName());
        emailField.setText(currentUser.getEmail());
        datePicker.setValue(currentUser.getBirthDate());
        loadUserAvatar(currentUser);
    }

    private void loadUserAvatar(User currentUser) {
        Image avatar = currentUser.getAvatar();
        if (avatar == null) {
            avatar = AvatarUtils.getDefaultAvatar();
        }

        AvatarUtils.applyCircularAvatar(avatarImageView, avatar);
        AvatarUtils.applyCircularAvatar(headerAvatarImageView, avatar);
    }

    private void applyStyles() {
        usernameLabel.getStyleClass().add("user-name");
        logoutButton.getStyleClass().add("profile-top-button");
        backButton.getStyleClass().add("profile-back-button");
        changeAvatarButton.getStyleClass().add("import-button");
        saveButton.getStyleClass().add("top-action-button");
    }

    private void setupValidation() {
        configureTooltips();
        emailField.textProperty().addListener((obs, oldVal, newVal) -> updateEmailError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordError());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDateError());
        updateEmailError();
        updatePasswordError();
        updateDateError();
    }

    private void configureTooltips() {
        mailErrorTooltip.setShowDelay(Duration.valueOf("200ms"));
        passwordErrorTooltip.setShowDelay(Duration.valueOf("200ms"));
        ageErrorTooltip.setShowDelay(Duration.valueOf("200ms"));
    }

    private void updateEmailError() {
        emailError.setVisible(!isEmailValid());
    }

    private void updatePasswordError() {
        passwordError.setVisible(!isPasswordValid());
    }

    private void updateDateError() {
        dateError.setVisible(!isBirthDateValid());
    }

    private boolean isEmailValid() {
        return User.checkEmail(emailField.getText().trim());
    }

    private boolean isPasswordValid() {
        String password = passwordField.getText();
        return password.isEmpty() || User.checkPassword(password);
    }

    private boolean isBirthDateValid() {
        LocalDate birthDate = datePicker.getValue();
        return birthDate != null && User.isOlderThan(birthDate, 12);
    }

    private boolean isFormValid() {
        return isEmailValid() && isPasswordValid() && isBirthDateValid();
    }

    private String getPasswordValue() {
        String password = passwordField.getText();
        if (password.isEmpty()) {
            return null;
        }
        return password;
    }

    @FXML
    private void handleChangeAvatar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un avatar");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }
        
        newAvatarPath = selectedFile.getAbsolutePath();
        Image newAvatar = new Image(selectedFile.toURI().toString());

        AvatarUtils.applyCircularAvatar(avatarImageView, newAvatar);
        AvatarUtils.applyCircularAvatar(headerAvatarImageView, newAvatar);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        updateEmailError();
        updatePasswordError();
        updateDateError();

        if (!isFormValid()) {
            showError("Datos inválidos", "Revisa los campos marcados con una exclamación.");
            return;
        }

        boolean updated = updateCurrentUser();
        if (!updated) {
            showError("Error", "No se pudieron guardar los cambios.");
            return;
        }

        AlertUtils.showInfo("Perfil actualizado", "Los cambios se han guardado correctamente.");
        navigateToHome(event);
    }

    private boolean updateCurrentUser() {
        String email = emailField.getText().trim();
        String password = getPasswordValue();
        LocalDate birthDate = datePicker.getValue();
        return app.updateCurrentUser(email, password, birthDate, newAvatarPath);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateToHome(event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtils.logoutAndNavigateToLogin(event);
    }

    private void navigateToHome(ActionEvent event) {
        NavigationUtils.navigateTo(event, NavigationTarget.to("/view/Home.fxml")
                .minSize(900, 600)
                .onError("No se pudo volver a la pantalla principal.")
                .build());
    }

    private void showError(String title, String text) {
        AlertUtils.showError(title, text);
    }
}
