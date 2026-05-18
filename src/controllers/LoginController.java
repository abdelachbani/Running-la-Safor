package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author abdel
 */
public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Hyperlink registerLink;
    @FXML
    private Button loginButton;

    SportActivityApp app = SportActivityApp.getInstance();
    @FXML
    private Text wrongCredentialsText;

    /**
     * Initializes the controller class.
     */
    @Override

    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void handleRegisterAction(ActionEvent event) {
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        if (!app.login(usernameField.getText(), passwordField.getText())) {
            wrongCredentialsText.setVisible(true);
        }
    }

}
