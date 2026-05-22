package controllers;

import utils.AlertUtils;
import utils.AvatarUtils;
import utils.NavigationUtils;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        NavigationUtils.navigateTo(event, "/view/Home.fxml",
                1000, 650, 1000, 650,
                "No se pudo volver a la pantalla principal.");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        app.logout();
        NavigationUtils.navigateTo(event, "/view/Login.fxml",
                640, 400, 640, 400,
                "No se pudo volver al login.");
    }
}