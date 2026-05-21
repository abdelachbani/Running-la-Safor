package controllers;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class SessionHistoryController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label screenTitleLabel;
    @FXML private Label usernameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Button logoutButton;
    @FXML private Button backButton;

    @FXML private TableView<SessionRow> sessionsTable;
    @FXML private TableColumn<SessionRow, String> startColumn;
    @FXML private TableColumn<SessionRow, String> endColumn;
    @FXML private TableColumn<SessionRow, String> durationColumn;
    @FXML private TableColumn<SessionRow, String> activitiesColumn;
    @FXML private TableColumn<SessionRow, String> notesColumn;

    @FXML private HBox totalRowBox;
    @FXML private Label totalTextLabel;
    @FXML private Label totalEndLabel;
    @FXML private Label totalDurationLabel;
    @FXML private Label totalActivitiesLabel;
    @FXML private Label totalNotesLabel;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTable();
        loadUserData();
        loadSessions();
        applyStyles();
    }

    private void configureTable() {
        startColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStart()));
        endColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEnd()));
        durationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDuration()));
        activitiesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActivities()));
        notesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNotes()));
    }

    private void loadUserData() {
        User currentUser = app.getCurrentUser();

        if (currentUser == null) {
            usernameLabel.setText("Usuario");
            avatarImageView.setImage(null);
            return;
        }

        usernameLabel.setText(currentUser.getNickName());

        Image avatar = currentUser.getAvatar();
        if (avatar != null) {
            avatarImageView.setImage(avatar);
        }
    }

    private void loadSessions() {
        User user = app.getCurrentUser();
        ObservableList<SessionRow> rows = FXCollections.observableArrayList();

        if (user == null) {
            sessionsTable.setItems(rows);
            updateTotals(0, 0, 0, 0);
            return;
        }

        var sessions = app.getSessionsByUser(user);

        long totalMinutes = 0;
        int totalImported = 0;
        int totalViewed = 0;
        int totalNotes = 0;

        for (Session session : sessions) {
            if (!isRelevantSession(session)) {
                continue;
            }

            LocalDateTime start = session.getStartTime();
            LocalDateTime end = session.getEndTime();
            int imported = session.getImportedActivities();
            int viewed = session.getViewedActivities();
            int notes = session.getAnnotationsCreated();
            long minutes = calculateMinutes(start, end);

            totalMinutes += minutes;
            totalImported += imported;
            totalViewed += viewed;
            totalNotes += notes;

            rows.add(new SessionRow(
                    formatDateTime(start),
                    formatDateTime(end),
                    formatDuration(minutes),
                    imported + "/" + viewed,
                    String.valueOf(notes)
            ));
        }

        sessionsTable.setItems(rows);
        updateTotals(totalMinutes, totalImported, totalViewed, totalNotes);
    }

    private boolean isRelevantSession(Session session) {
        return hasActivityData(session) || hasUsefulDuration(session);
    }

    private boolean hasActivityData(Session session) {
        return session.getImportedActivities() > 0
                || session.getViewedActivities() > 0
                || session.getAnnotationsCreated() > 0;
    }

    private boolean hasUsefulDuration(Session session) {
        LocalDateTime start = session.getStartTime();
        LocalDateTime end = session.getEndTime();
        long minutes = calculateMinutes(start, end);
        return minutes > 1;
    }

    private long calculateMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }

        return Math.max(0, Duration.between(start, end).toMinutes());
    }

    private void updateTotals(long totalMinutes, int totalImported, int totalViewed, int totalNotes) {
        totalDurationLabel.setText(formatDuration(totalMinutes));
        totalActivitiesLabel.setText(totalImported + "/" + totalViewed);
        totalNotesLabel.setText(String.valueOf(totalNotes));
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(formatter);
    }

    private String formatDuration(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    private void applyStyles() {
        titleLabel.getStyleClass().add("screen-title");
        screenTitleLabel.getStyleClass().add("screen-title");
        usernameLabel.getStyleClass().add("user-name");
        logoutButton.getStyleClass().add("top-action-button");
        backButton.getStyleClass().add("top-action-button");
        sessionsTable.getStyleClass().add("activities-table");
        totalRowBox.getStyleClass().add("history-total-row");
        totalTextLabel.getStyleClass().add("history-total-cell");
        totalEndLabel.getStyleClass().add("history-total-cell");
        totalDurationLabel.getStyleClass().add("history-total-cell");
        totalActivitiesLabel.getStyleClass().add("history-total-cell");
        totalNotesLabel.getStyleClass().add("history-total-cell");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        openView(event, "/view/Home.fxml", 900, 600, "No se pudo volver a la pantalla principal.");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        app.logout();
        openView(event, "/view/Login.fxml", 400, 400, "No se pudo volver a la pantalla de login.");
    }

    private void openView(ActionEvent event, String fxmlPath, double minWidth, double minHeight, String errorText) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);
            stage.show();
        } catch (IOException e) {
            showError("Error", errorText);
        }
    }

    private void showError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static class SessionRow {
        private final String start;
        private final String end;
        private final String duration;
        private final String activities;
        private final String notes;

        public SessionRow(String start, String end, String duration, String activities, String notes) {
            this.start = start;
            this.end = end;
            this.duration = duration;
            this.activities = activities;
            this.notes = notes;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }

        public String getDuration() {
            return duration;
        }

        public String getActivities() {
            return activities;
        }

        public String getNotes() {
            return notes;
        }
    }
}