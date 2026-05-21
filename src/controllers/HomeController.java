package controllers;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class HomeController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Button logoutButton;

    @FXML
    private Button importButton;

    @FXML
    private Label activitiesCountLabel;

    @FXML
    private Label lastActivityLabel;

    @FXML
    private TableView<Activity> activitiesTable;

    @FXML
    private TableColumn<Activity, String> nameColumn;

    @FXML
    private TableColumn<Activity, String> dateColumn;

    @FXML
    private TableColumn<Activity, String> distanceColumn;

    @FXML
    private Button openButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button sessionsButton;

    @FXML
    private Button mapsButton;

    @FXML
    private Button accumulatedButton;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTable();
        loadUserData();
        loadActivities();
        applyStyles();
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getName())
        );

        dateColumn.setCellValueFactory(cellData -> {
            Activity activity = cellData.getValue();
            String value = activity.getStartTime() != null
                    ? activity.getStartTime().toLocalDate().format(dateFormatter)
                    : "-";
            return new SimpleStringProperty(value);
        });

        distanceColumn.setCellValueFactory(cellData -> {
            double km = cellData.getValue().getTotalDistance() / 1000.0;
            return new SimpleStringProperty(String.format("%.1f km", km));
        });
    }

    private void loadUserData() {
        User currentUser = app.getCurrentUser();
        if (currentUser == null) {
            usernameLabel.setText("Usuario");
            AvatarUtils.applyCircularAvatar(avatarImageView, AvatarUtils.getDefaultAvatar());
            return;
        }

        usernameLabel.setText(currentUser.getNickName());

        Image avatar = currentUser.getAvatar();
        if (avatar == null) {
            avatar = AvatarUtils.getDefaultAvatar();
        }

        AvatarUtils.applyCircularAvatar(avatarImageView, avatar);
    }

    private void loadActivities() {
        var activities = app.getUserActivities();
        activitiesTable.getItems().setAll(activities);

        activitiesCountLabel.setText("Actividades: " + activities.size());

        if (activities.isEmpty()) {
            lastActivityLabel.setText("Última actividad: -");
        } else {
            lastActivityLabel.setText("Última actividad: " + activities.get(0).getName());
        }
    }

    private void applyStyles() {
        titleLabel.getStyleClass().add("screen-title");
        usernameLabel.getStyleClass().add("user-name");
        activitiesCountLabel.getStyleClass().add("summary-label");
        lastActivityLabel.getStyleClass().add("summary-label");
        logoutButton.getStyleClass().add("profile-top-button");
        importButton.getStyleClass().add("import-button");
        openButton.getStyleClass().add("table-action-button");
        deleteButton.getStyleClass().add("table-action-button");
        profileButton.getStyleClass().add("profile-menu-button");
        sessionsButton.getStyleClass().add("profile-menu-button");
        mapsButton.getStyleClass().add("profile-menu-button");
        accumulatedButton.getStyleClass().add("profile-menu-button");
        activitiesTable.getStyleClass().add("activities-table");
    }

    @FXML
    private void handleOpenActivity(ActionEvent event) {
        Activity selected = activitiesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Selecciona una actividad", "Debes seleccionar una actividad para abrirla.");
            return;
        }

        showInfo("Pendiente", "La pantalla de visualización de actividad será la siguiente que conectemos.");
    }

    @FXML
    private void handleDeleteActivity(ActionEvent event) {
        Activity selected = activitiesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Selecciona una actividad", "Debes seleccionar una actividad para eliminarla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar borrado");
        confirm.setHeaderText("Eliminar actividad");
        confirm.setContentText("¿Quieres eliminar \"" + selected.getName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            app.removeActivity(selected);
            loadActivities();
        }
    }

    @FXML
    private void handleImportActivity(ActionEvent event) {
        URL importView = getClass().getResource("/view/ImportActivity.fxml");
        URL styles = getClass().getResource("/resources/styles.css");
        
        if (importView == null || styles == null) {
            showError("Error", "No se pudo abrir la pantalla de importar actividad.");
            return;
        }

        try {
            Parent root = FXMLLoader.load(importView);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(styles.toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(600);
            stage.setMinHeight(420);
            stage.show();
        } catch (IOException ex) {
            showError("Error", "No se pudo abrir la pantalla de importar actividad.");
        }
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/EditProfile.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (IOException e) {
            showError("Error", "No se pudo abrir la pantalla de perfil.");
        }
    }

    @FXML
    private void handleSessions(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/SessionHistory.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (IOException e) {
            showError("Error", "No se pudo abrir el historial de sesiones.");
        }
    }

    @FXML
    private void handleMaps(ActionEvent event) {
        showInfo("Pendiente", "La pantalla de gestión de mapas será la siguiente que conectemos.");
    }

    @FXML
    private void handleAccumulated(ActionEvent event) {
        showInfo("Pendiente", "La pantalla de acumulado será la siguiente que conectemos.");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        app.logout();

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
            showError("Error", "No se pudo volver a la pantalla de login.");
        }
    }

    private void showInfo(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showWarning(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}