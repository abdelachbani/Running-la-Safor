package controllers;

import application.AvatarUtils;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

// AI generated
public class SessionHistoryController implements Initializable {

    @FXML private TableView<Session> sessionsTable;
    @FXML private TableColumn<Session, String> startColumn;
    @FXML private TableColumn<Session, String> endColumn;
    @FXML private TableColumn<Session, String> durationColumn;
    @FXML private TableColumn<Session, String> importedColumn;
    @FXML private TableColumn<Session, String> viewedColumn;
    @FXML private TableColumn<Session, String> annotationsColumn;

    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Button logoutButton;
    @FXML private Button backButton;

    @FXML private Label totalSessionsLabel;
    @FXML private Label totalImportedLabel;
    @FXML private Label totalViewedLabel;
    @FXML private Label totalAnnotationsLabel;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private final DateTimeFormatter dtFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTable();
        loadSessions();
        loadUserData();
        applyStyles();
    }

    private void configureTable() {
        startColumn.setCellValueFactory(cell -> {
            Session s = cell.getValue();
            String value = s.getStartTime() != null
                    ? s.getStartTime().format(dtFormatter)
                    : "-";
            return new SimpleStringProperty(value);
        });

        endColumn.setCellValueFactory(cell -> {
            Session s = cell.getValue();
            String value = s.getEndTime() != null
                    ? s.getEndTime().format(dtFormatter)
                    : "-";
            return new SimpleStringProperty(value);
        });

        durationColumn.setCellValueFactory(cell -> {
            Session s = cell.getValue();
            long totalSeconds = s.getDuration().getSeconds();
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            String value = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            return new SimpleStringProperty(value);
        });

        importedColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(
                String.valueOf(cell.getValue().getImportedActivities())));

        viewedColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(
                String.valueOf(cell.getValue().getViewedActivities())));

        annotationsColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(
                String.valueOf(cell.getValue().getAnnotationsCreated())));
    }

    private void loadSessions() {
        User currentUser = app.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        List<Session> sessions = app.getSessionsByUser(currentUser);
        sessionsTable.getItems().setAll(sessions);

        totalSessionsLabel.setText("Total sesiones: " + sessions.size());

        int totalImported = sessions.stream().mapToInt(Session::getImportedActivities).sum();
        int totalViewed = sessions.stream().mapToInt(Session::getViewedActivities).sum();
        int totalAnnotations = sessions.stream().mapToInt(Session::getAnnotationsCreated).sum();

        totalImportedLabel.setText("Actividades importadas (total): " + totalImported);
        totalViewedLabel.setText("Actividades vistas (total): " + totalViewed);
        totalAnnotationsLabel.setText("Anotaciones creadas (total): " + totalAnnotations);
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

    private void applyStyles() {
        titleLabel.getStyleClass().add("screen-title");
        usernameLabel.getStyleClass().add("user-name");
        logoutButton.getStyleClass().add("profile-top-button");
        backButton.getStyleClass().add("profile-back-button");
        sessionsTable.getStyleClass().add("activities-table");
    }

    @FXML
private void handleBack(ActionEvent event) {
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(650);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.centerOnScreen();
        stage.show();
    } catch (IOException e) {
        showError("Error", "No se pudo volver a la pantalla principal.");
    }
}

    @FXML
private void handleLogout(ActionEvent event) {
    app.logout();
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Scene scene = new Scene(root, 640, 400);
        scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.setWidth(640);
        stage.setHeight(400);
        stage.setMinWidth(640);
        stage.setMinHeight(400);
        stage.centerOnScreen();
        stage.show();
    } catch (IOException e) {
        showError("Error", "No se pudo volver al login.");
    }
}

    private void showError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}