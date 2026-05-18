package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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
