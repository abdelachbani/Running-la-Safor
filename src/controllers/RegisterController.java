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
import upv.ipc.sportlib.User;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupValidation();
    }

    private void setupValidation() {
        // Tooltips with validation rules
        usernameError.setTooltip(new Tooltip("Entre 6 y 15 caracteres, solo letras, dígitos, guion o subguion."));
        emailError.setTooltip(new Tooltip("Formato de email no válido (usuario@dominio)."));
        passwordError.setTooltip(new Tooltip("Entre 8 y 20 caracteres, al menos una mayúscula, una minúscula, un dígito y un símbolo (!@#$%&*()-+=)."));
        dateError.setTooltip(new Tooltip("Debes tener más de 12 años."));

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
    }

    @FXML
    private void onRegisterClick(ActionEvent event) {
    }

    @FXML
    private void handleLoginAction(ActionEvent event) {
    }

}
