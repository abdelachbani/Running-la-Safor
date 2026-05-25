package controllers;

import utils.AvatarUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import utils.AlertUtils;
import utils.ui_navigation.NavigationTarget;
import utils.ui_navigation.NavigationUtils;

/**
 * FXML Controller class
 *
 * @author Ikerc
 */
public class ImportActivityController implements Initializable {

    @FXML
    private Label usernameLabel;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private TextField txtFileRoute;
    @FXML
    private Button btnExamine;
    @FXML
    private Button btnImport;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private File selectedFile = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (app.getCurrentUser() != null) {
            usernameLabel.setText(app.getCurrentUser().getNickName());
            loadAvatar();
        }
    }

    private void loadAvatar() {
        Image avatar = app.getCurrentUser().getAvatar();
        if (avatar == null) {
            avatar = AvatarUtils.getDefaultAvatar();
        }

        AvatarUtils.applyCircularAvatar(avatarImageView, avatar);
    }

    @FXML
    private void handleExamine(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar fichero GPX");

        // Filtro para mostrar solo archivos .gpx
        FileChooser.ExtensionFilter gpxFilter
                = new FileChooser.ExtensionFilter("Ficheros GPX (*.gpx)", "*.gpx");
        fileChooser.getExtensionFilters().add(gpxFilter);

        // Obtener el Stage actual desde el evento
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedFile = file;
            txtFileRoute.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleImport(ActionEvent event) {

        if (selectedFile == null || txtFileRoute.getText().isBlank()) {
            AlertUtils.showError("Error", "Por favor, selecciona un fichero GPX antes de importar.");
            return;
        }

        // Validar que el archivo existe y tiene extensión .gpx
        if (!selectedFile.exists() || !selectedFile.getName().endsWith(".gpx")) {
            AlertUtils.showError("Error", "El fichero seleccionado no es válido o no existe.");
            return;
        }

        try {

            Activity activity = app.importActivity(selectedFile);

            if (activity != null) {
                AlertUtils.showInfo("Éxito", "Actividad \"" + activity.getName() + "\" importada correctamente.");

            } else {
                AlertUtils.showError("Error", "No se pudo importar la actividad. Comprueba que el fichero GPX es válido.");
            }

        } catch (Exception e) {
            AlertUtils.showError("Error", "Error al importar: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        NavigationUtils.logoutAndNavigateToLogin(event);
    }

    @FXML
    private void handleReturn(ActionEvent event) {
        NavigationUtils.navigateTo(event,
                NavigationTarget.to("/view/Home.fxml")
                        .minSize(900, 600)
                        .onError("No se pudo volver al menú principal.")
                        .build());
    }

    public void handleWindowChange(String path, int minWidth, int minHeight, ActionEvent event) {
        URL homeView = getClass().getResource(path);
        URL styles = getClass().getResource("/resources/styles.css");

        if (homeView == null || styles == null) {
            AlertUtils.showError("Error", "No se pudo volver a la pantalla principal.");
            return;
        }

        try {
            Parent root = FXMLLoader.load(homeView);
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

            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);

            if (!wasMaximized) {
                stage.centerOnScreen();
            }
            stage.show();
        } catch (IOException ex) {
            AlertUtils.showError("Error", "No se pudo volver a la pantalla principal.");
        }
    }
}
