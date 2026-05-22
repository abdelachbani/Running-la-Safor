package controllers;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class AccumulatedController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Button logoutButton;
    @FXML private Button backButton;
    @FXML private Button periodButton;

    @FXML private Label activitiesCountLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label totalDistanceLabel;
    @FXML private Label totalGainLabel;
    @FXML private Label totalLossLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<Activity> monthlyActivitiesTable;
    @FXML private TableColumn<Activity, String> nameColumn;
    @FXML private TableColumn<Activity, String> dateColumn;
    @FXML private TableColumn<Activity, String> distanceColumn;
    @FXML private TableColumn<Activity, String> durationColumn;
    @FXML private TableColumn<Activity, String> gainColumn;
    @FXML private TableColumn<Activity, String> lossColumn;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Locale spanishLocale = new Locale("es", "ES");

    private YearMonth selectedPeriod = YearMonth.of(2026, 3);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        titleLabel.getStyleClass().add("screen-title");
        usernameLabel.getStyleClass().add("user-name");
        logoutButton.getStyleClass().add("top-action-button");
        backButton.getStyleClass().add("nav-button");
        periodButton.getStyleClass().add("import-button");

        activitiesCountLabel.getStyleClass().add("summary-label");
        totalTimeLabel.getStyleClass().add("summary-label");
        totalDistanceLabel.getStyleClass().add("summary-label");
        totalGainLabel.getStyleClass().add("summary-label");
        totalLossLabel.getStyleClass().add("summary-label");
        statusLabel.getStyleClass().add("summary-label");

        configureTable();
        loadUserData();
        updatePeriodButtonText();
        loadAccumulatedData();
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeText(cell.getValue().getName())));

        dateColumn.setCellValueFactory(cell -> {
            Activity activity = cell.getValue();
            String value = activity.getStartTime() != null
                    ? activity.getStartTime().toLocalDate().format(dateFormatter)
                    : "-";
            return new SimpleStringProperty(value);
        });

        distanceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatDistance(cell.getValue().getTotalDistance())));

        durationColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatDuration(cell.getValue().getDuration())));

        gainColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format(Locale.ROOT, "%.0f m", cell.getValue().getElevationGain())));

        lossColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format(Locale.ROOT, "%.0f m", cell.getValue().getElevationLoss())));

        monthlyActivitiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        monthlyActivitiesTable.setPlaceholder(new Label("No hay actividades en este periodo."));
    }

    private void updatePeriodButtonText() {
        String monthName = selectedPeriod.getMonth().getDisplayName(TextStyle.FULL, spanishLocale);
        monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);
        periodButton.setText(monthName + " " + selectedPeriod.getYear());
    }

    private void loadAccumulatedData() {
        List<Activity> allActivities = app.getUserActivities();

        List<Activity> filtered = new ArrayList<>();
        for (Activity activity : allActivities) {
            if (activity != null
                    && activity.getStartTime() != null
                    && YearMonth.from(activity.getStartTime()).equals(selectedPeriod)) {
                filtered.add(activity);
            }
        }

        monthlyActivitiesTable.getItems().setAll(filtered);

        double totalDistance = 0.0;
        double totalGain = 0.0;
        double totalLoss = 0.0;
        Duration totalDuration = Duration.ZERO;

        for (Activity activity : filtered) {
            totalDistance += activity.getTotalDistance();
            totalGain += activity.getElevationGain();
            totalLoss += activity.getElevationLoss();

            if (activity.getDuration() != null) {
                totalDuration = totalDuration.plus(activity.getDuration());
            }
        }

        activitiesCountLabel.setText("Actividades: " + filtered.size());
        totalTimeLabel.setText("Tiempo total: " + formatDuration(totalDuration));
        totalDistanceLabel.setText("Distancia total: " + formatDistance(totalDistance));
        totalGainLabel.setText(String.format(Locale.ROOT, "Ascenso total: %.0f m", totalGain));
        totalLossLabel.setText(String.format(Locale.ROOT, "Descenso total: %.0f m", totalLoss));

        if (filtered.isEmpty()) {
            statusLabel.setText("No hay actividades en " + periodDescription());
        } else {
            statusLabel.setText("Mostrando actividades de " + periodDescription());
        }
    }

    @FXML
    private void handlePeriodSelection(ActionEvent event) {
        ComboBox<Month> monthCombo = new ComboBox<>(FXCollections.observableArrayList(Month.values()));
        monthCombo.setValue(selectedPeriod.getMonth());
        monthCombo.setMaxWidth(Double.MAX_VALUE);
        monthCombo.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Month item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : capitalize(item.getDisplayName(TextStyle.FULL, spanishLocale)));
            }
        });
        monthCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Month item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : capitalize(item.getDisplayName(TextStyle.FULL, spanishLocale)));
            }
        });

        int currentYear = 2026;
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 3; y <= currentYear + 3; y++) {
            years.add(y);
        }

        ComboBox<Integer> yearCombo = new ComboBox<>(FXCollections.observableArrayList(years));
        yearCombo.setValue(selectedPeriod.getYear());
        yearCombo.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.Dialog<YearMonth> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Seleccionar periodo");
        dialog.setHeaderText("Elige mes y año");

        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Mes:"), 0, 0);
        grid.add(monthCombo, 1, 0);
        grid.add(new Label("Año:"), 0, 1);
        grid.add(yearCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == javafx.scene.control.ButtonType.OK) {
                Integer year = yearCombo.getValue();
                Month month = monthCombo.getValue();
                if (year != null && month != null) {
                    return YearMonth.of(year, month);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(period -> {
            selectedPeriod = period;
            updatePeriodButtonText();
            loadAccumulatedData();
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (IOException ex) {
            showError("No se pudo volver a la pantalla principal.");
        }
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
        } catch (IOException ex) {
            showError("No se pudo cerrar sesión.");
        }
    }

    private void loadUserData() {
        User currentUser = app.getCurrentUser();
        if (currentUser == null) {
            usernameLabel.setText("Usuario");
            avatarImageView.setImage(null);
            return;
        }

        usernameLabel.setText(currentUser.getNickName());
        avatarImageView.setImage(currentUser.getAvatar());
    }

    private String periodDescription() {
        String monthName = selectedPeriod.getMonth().getDisplayName(TextStyle.FULL, spanishLocale);
        return capitalize(monthName) + " " + selectedPeriod.getYear();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private String formatDistance(double meters) {
        return String.format(Locale.ROOT, "%.2f km", meters / 1000.0);
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "-";
        }
        long seconds = duration.getSeconds();
        long abs = Math.abs(seconds);
        return String.format(Locale.ROOT, "%d:%02d:%02d", abs / 3600, (abs % 3600) / 60, abs % 60);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}