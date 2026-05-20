package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import upv.ipc.sportlib.User;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author abdel
 */
public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField usernameField11;
    @FXML
    private Button browseButton;
    @FXML
    private Button registerButton;
    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label usernameError;
    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;
    @FXML
    private Label dateError;
    @FXML
    private Tooltip usernameErrorTooltip;
    @FXML
    private Tooltip mailErrorTooltip;
    @FXML
    private Tooltip passwordErrorTooltip;
    @FXML
    private Tooltip ageErrorTooltip;
    
    private final SportActivityApp app = SportActivityApp.getInstance();
    private String avatarPath = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupValidation();
    }

    private void setupValidation() {
        // Tooltips with validation rules
        usernameErrorTooltip.setShowDelay(Duration.valueOf("200ms"));
        mailErrorTooltip.setShowDelay(Duration.valueOf("200ms"));
        passwordErrorTooltip.setShowDelay(Duration.valueOf("200ms"));
        ageErrorTooltip.setShowDelay(Duration.valueOf("200ms"));

        // Listeners for real-time validation
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            usernameError.setVisible(!User.checkNickName(newVal));
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            emailError.setVisible(!User.checkEmail(newVal));
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            passwordError.setVisible(!User.checkPassword(newVal));
        });

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                dateError.setVisible(!User.isOlderThan(newVal, 12));
            } else {
                dateError.setVisible(true);
            }
        });
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un avatar");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            avatarPath = selectedFile.getAbsolutePath();
            usernameField11.setText(avatarPath);
        }
    }

    @FXML
    private void onRegisterClick(ActionEvent event) {
        String nick = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        LocalDate birthDate = datePicker.getValue();

        boolean validNick = User.checkNickName(nick);
        boolean validEmail = User.checkEmail(email);
        boolean validPassword = User.checkPassword(password);
        boolean validDate = birthDate != null && User.isOlderThan(birthDate, 12);

        usernameError.setVisible(!validNick);
        emailError.setVisible(!validEmail);
        passwordError.setVisible(!validPassword);
        dateError.setVisible(!validDate);

        boolean invalidData = !validNick || !validEmail || !validPassword || !validDate;
        
        if (invalidData) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Datos inválidos");
            alert.setContentText("Revisa los campos del formulario.");
            alert.showAndWait();
            return;
        }

        boolean registered = app.registerUser(nick, email, password, birthDate, avatarPath);

        Alert alert = new Alert(registered ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(registered ? "Registro correcto" : "Error");
        alert.setHeaderText(null);
        alert.setContentText(
        registered
            ? "Usuario registrado correctamente."
            : "No se pudo registrar. Puede que el nickname ya exista."
        );
        alert.showAndWait();

        if (registered) {
            handleLoginAction(event);
        }
    }

    @FXML
    private void handleLoginAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir login");
            alert.setContentText("Revisa la ruta del FXML o del CSS.");
            alert.showAndWait();
    }
}

}
