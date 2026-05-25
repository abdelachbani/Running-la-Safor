package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import upv.ipc.sportlib.SportActivityApp;
import javafx.stage.Stage;
import javafx.scene.Node;
import utils.ui_navigation.NavigationTarget;
import utils.ui_navigation.NavigationUtils;

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
        URL registerView = getClass().getResource("/view/Register.fxml");
        URL styles = getClass().getResource("/resources/styles.css");
        if (registerView == null || styles == null) {
            showRegisterLoadError();
            return;
        }

        try {
            Parent root = FXMLLoader.load(registerView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();

            if (currentScene != null) {
                currentScene.setRoot(root);
                currentScene.getStylesheets().setAll(styles.toExternalForm());
            } else {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(styles.toExternalForm());
                stage.setScene(scene);
            }

            stage.setMinWidth(400);
            stage.setMinHeight(500);

            if (!wasMaximized) {
                stage.centerOnScreen();
            }
            stage.show();
        } catch (IOException ex) {
            showRegisterLoadError();
        }
    }

    private void showRegisterLoadError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("No se pudo abrir la pantalla de registro");
        alert.setContentText("Inténtalo de nuevo más tarde.");
        alert.showAndWait();
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        if (!app.login(usernameField.getText(), passwordField.getText())) {
            wrongCredentialsText.setVisible(true);
        } else {
            NavigationUtils.navigateTo(event, NavigationTarget.to("/view/Home.fxml")
                    .minSize(1000, 700)
                    .onError("No se pudo ir a la pantalla principal.")
                    .build());
        }
    }

}
